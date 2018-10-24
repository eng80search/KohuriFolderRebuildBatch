package kohuri.folderrebuild.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import kohuri.folderrebuild.common.constantCommon;
import kohuri.folderrebuild.common.enumCommon.BatchLogLevel;
import kohuri.folderrebuild.common.propertyCommon;
import kohuri.folderrebuild.util.CsvFileSort;
import kohuri.folderrebuild.util.CsvUtil;
import kohuri.folderrebuild.util.LoggerUtil;
import kohuri.folderrebuild.util.ParamUtil;

/*
 * メイン処理クラス
 * CSVファイルからフォルダ再構築情報を読み取り、設定ファイルからの情報に
 * 基づいてフォルダの再構築及び統計情報を出力する。
*/
/**
 * @author user1
 *
 */
public class FolderRebuild {
	// ログ用
	static LoggerUtil batchLog = new LoggerUtil();

	// プロパティ保存用
	propertyCommon collectionProperty = new propertyCommon();

	// メイン処理スタート時点
	public static void main(String[] args) {

		FolderRebuild folderRebuild = new FolderRebuild();
		folderRebuild.runRebuildProcess();

	}

	/**
	 *
	 *メイン処理本体
	 */
	public int runRebuildProcess() {

		// 初期処理

		// 初期化：sendFolderName batchsets直下の送信フォルダ名=Empty
		String scanFolderRootPath = null; /*scanフォルダルートパス*/
		String exportFolderRootPath = null; /*exportフォルダルートパス*/
		String sendFolderRootPath = null; /*送信フォルダルートパス*/
		String currentSoshinFolderName = ""; /*送信フォルダ名*/
		String oldSoshinFolderName = ""; /*送信フォルダ名*/
		String scanReadDate = null; /*スキャン読み取り日*/
		String batchNo = null; /*バッチ番号*/
		String batchSerialNo = null; /*バッチ連番*/
		String recordSerialNo = null; /*レコード連番*/
		String itakuSyaCode = null; /*委託者コード*/
		String imgFileName = null; /*イメージファイル名（パスなし）*/
		String imgFileNameFullPath = null; /*イメージファイル名（パスあり）*/

		//csvデータ読み取り用
		List<String[]> readAllCsvList = new ArrayList<String[]>();
		List<CsvFileSort> readAllSortedCsvList = new ArrayList<CsvFileSort>();

		//2次OCRデータ書き込み用
		List<String[]> nijiOcrCsvList = new ArrayList<String[]>();
		int csvDataCnt = 0; //2次OCRデータ書き込み件数

		// 初期化：プロパティファイル値取得用
		String propInputRootPath = null;
		String propOutputRootPath = null;
		String propInfoCsvFileName = null;

		try {
			// ①設定ファイルから設定値を取得する。
			collectionProperty = getPropertySettingValue();
			propInputRootPath = collectionProperty.getPropInputFilePath();
			propOutputRootPath = collectionProperty.getPropOutputFilePath();
			propInfoCsvFileName = collectionProperty.getPropInfoCsvFileName();

			// Debug
			batchLog.writerLog("---------バッチ実行開始-----------" , BatchLogLevel.INFO);
			batchLog.writerLog("JARファイルバージョン：" + constantCommon.JAR_VERSION, BatchLogLevel.INFO);
			batchLog.writerLog("取り込み元ルートパス：" + propInputRootPath, BatchLogLevel.INFO);
			batchLog.writerLog("出力先ルートパス：" + propOutputRootPath, BatchLogLevel.INFO);
			batchLog.writerLog("取り込み用CSVファイル：" + propInfoCsvFileName, BatchLogLevel.INFO);

			// ②出力先にフォルダ階層を構築する。
			initCreateOutputDirectory(propOutputRootPath);
			scanFolderRootPath = propOutputRootPath + "\\" + constantCommon.DIR_LV1_IMPORTS
					+ "\\" + constantCommon.DIR_LV2_SCAN;
			sendFolderRootPath = propOutputRootPath + "\\" + constantCommon.DIR_LV1_IMPORTS
					+ "\\" + constantCommon.DIR_LV2_BATCHSETS;
			exportFolderRootPath = propOutputRootPath + "\\" + constantCommon.DIR_LV1_EXPORTS;

			// ③Loop処理:CSVファイルを一行読み取る。(レコードが何万件なので、仕方なくここに書く)

			CSVReader reader = new CSVReader(new FileReader(propInfoCsvFileName));

			//ソートのために一気にCSVファイルを読み取りする。
			readAllCsvList = reader.readAll();
			//ヘッダは削除する。
			if (readAllCsvList.size() > 0)
				readAllCsvList.remove(0);

			//バッチの件数
			batchLog.writerLog("CSVファイル全件数：" + readAllCsvList.size()
								, BatchLogLevel.INFO);

			//事前処理：スキップ処理を行う
			//(関数実行後、readAllCsvListの不要なデータは削除される)。
			removeSkipCsvData(readAllCsvList);

			for (String[] csvValueArray : readAllCsvList) {

				CsvFileSort oneRowCsvData = new CsvFileSort();

				for (int i = 0; i < csvValueArray.length; i++) {

					//必要以上読まない
					if (i > constantCommon.CSV_INDEX_JPGNAME)
						break;

					switch (i) {
					//バッチ番号
					case constantCommon.CSV_INDEX_BATCHNO:
						batchNo = getFormattedBatchNo(csvValueArray[i]); //左0埋め
						oneRowCsvData.setBatchNo(batchNo);

						//						batchLog.writerLog("バッチ番号：" + batchNo, BatchLogLevel.TRACE);
						break;
					//バッチ連番
					case constantCommon.CSV_INDEX_BATCH_SERIALNO:
						batchSerialNo = getFormattedBatchSerialNo(csvValueArray[i]);
						oneRowCsvData.setBatchSerialNo(batchSerialNo);

						//						batchLog.writerLog("バッチ連番：" + batchSerialNo, BatchLogLevel.TRACE);
						break;
					//レコード連番（送信完了ファイルの回次に使用される）
					case constantCommon.CSV_INDEX_RECORD_SERIALNO:
						recordSerialNo = getFormattedRecordSerialNo(csvValueArray[i]);
						oneRowCsvData.setRecordSerialNo(recordSerialNo);

						//						batchLog.writerLog("レコード連番：" + recordSerialNo, BatchLogLevel.TRACE);
						break;
					//スキャナ読取日
					case constantCommon.CSV_INDEX_SCANDATE:
						scanReadDate = csvValueArray[i];
						oneRowCsvData.setScanReadDate(scanReadDate);

						//						batchLog.writerLog("スキャナ読取日：" + scanReadDate, BatchLogLevel.TRACE);
						break;
					//委託者コード
					case constantCommon.CSV_INDEX_ITAKUSYACODE:
						itakuSyaCode = getFormattedItakkusyaCode(csvValueArray[i]);
						oneRowCsvData.setItakuSyaCode(itakuSyaCode);

						//						batchLog.writerLog("委託者コード：" + itakuSyaCode, BatchLogLevel.TRACE);
						break;
					//イメージファイル名
					case constantCommon.CSV_INDEX_JPGNAME:
						imgFileName = csvValueArray[i];
						oneRowCsvData.setImgFileName(imgFileName);

						//						batchLog.writerLog("イメージファイル名：" + imgFileName, BatchLogLevel.TRACE);
						break;
					default:

					}
				}

				//一行のデータを追加する。
				readAllSortedCsvList.add(oneRowCsvData);
			}

			// CSV全データを読み終わったのでソートする（効果がない？？）
			Collections.sort(readAllSortedCsvList);

			//テスト用コード
			//			String testValue = null;
			//			for(CsvFileSort clsValue:readAllSortedCsvList) {
			//				testValue = "";
			//				testValue += clsValue.getScanReadDate() + clsValue.getBatchNo()
			//					+ clsValue.getRecordSerialNo() + clsValue.getItakuSyaCode();
			//				batchLog.writerLog("ソート済データ出力", BatchLogLevel.TRACE);
			//				batchLog.writerLog(testValue, BatchLogLevel.TRACE);
			//			}

			//ループ処理（ソート済のCSVデータ）
			// 処理：フォルダ再構築に必要な情報を取得する。
			for (CsvFileSort csvData : readAllSortedCsvList) {

				//バッチ番号
				batchNo = csvData.getBatchNo(); //左0埋め
				batchLog.writerLog("バッチ番号：" + batchNo, BatchLogLevel.TRACE);

				//バッチ連番
				batchSerialNo = csvData.getBatchSerialNo();
				batchLog.writerLog("バッチ連番：" + batchSerialNo, BatchLogLevel.TRACE);

				//レコード連番（送信完了ファイルの回次に使用される）
				recordSerialNo = csvData.getRecordSerialNo();
				batchLog.writerLog("レコード連番：" + recordSerialNo, BatchLogLevel.TRACE);

				//スキャナ読取日
				scanReadDate = csvData.getScanReadDate();
				batchLog.writerLog("スキャナ読取日：" + scanReadDate, BatchLogLevel.TRACE);

				//委託者コード
				itakuSyaCode = csvData.getItakuSyaCode();
				batchLog.writerLog("委託者コード：" + itakuSyaCode, BatchLogLevel.TRACE);

				//イメージファイル名
				imgFileName = csvData.getImgFileName();
				batchLog.writerLog("イメージファイル名：" + imgFileName, BatchLogLevel.TRACE);

				// 処理１：前のフォルダ情報を退避する。
				oldSoshinFolderName = new String(currentSoshinFolderName.toString());

				// 処理２.送信フォルダ名を取得する。
				currentSoshinFolderName = getSenderFolderName(sendFolderRootPath, scanReadDate, batchNo, recordSerialNo,
						itakuSyaCode);

				// 処理３：イメージファイルをbatchsets送信フォルダにコピーする(上書き方式)
				File sourceImageDirectory = new File(propInputRootPath + "\\" + scanReadDate);

				// 処理４：イメージソースファイル名を取得する。
				batchLog.writerLog("sourceImageDirectory：" + sourceImageDirectory.getPath(), BatchLogLevel.TRACE);
				imgFileNameFullPath = findFileInSubdirectory(sourceImageDirectory, imgFileName);

				// 判断A：前回の結果CSVファイルを作成必要があったら作成する。
				// 判断基準：前回退避した送信フォルダがnullでない且つ今回の送信フォルダ名と
				//			 違うとき且つ前回コピーが行われている時である。
				if (!oldSoshinFolderName.equals("") && !oldSoshinFolderName.equals(currentSoshinFolderName)
						&& nijiOcrCsvList.size() > 0) {

					//2次OCR対象ファイル、受信監視ファイル、送信完了ファイル名作成用のTimeStampを作成
					Calendar rightNow = Calendar.getInstance();

					//フォーマットパターンを指定して表示する
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					String fileTimeStamp = sdf.format(rightNow.getTime());

					// 処理1:2次OCR対象データをまとめて書き込む
					csvDataCnt = writeCsvDataToNijiOcrCsvFile(oldSoshinFolderName, nijiOcrCsvList);

					// 処理2:exportsフォルダに空の受信監視ファイルを作成する。
					makeFileJyusinKansiFolder(exportFolderRootPath, oldSoshinFolderName, fileTimeStamp);

					// 処理3:imports\scanフォルダの直下に送信完了ファイルを作成する。
					makeFileSosinKanryoFolder(scanFolderRootPath, oldSoshinFolderName, fileTimeStamp,
							csvDataCnt);

					// 処理4:2次OCR対象書き込み用Listを初期化する。
					nijiOcrCsvList.clear();
				}

				//重要！！コピー元イメージソースファイルがなければ後続のフォルダ再構築処理は行わない。
				if (imgFileNameFullPath == null) {

					continue;
				}

				batchLog.writerLog("currentSoshinFolderName：" + currentSoshinFolderName, BatchLogLevel.TRACE);

				// 判断：batchestsの直下に送信フォルダサブディレクトリが存在するか
				if (!exsistDirectory(currentSoshinFolderName)) {
					// 存在しない場合:

					// 処理１：新規フォルダを作成する。
					String newFolder[] = { currentSoshinFolderName };
					createDirectory(newFolder);
					batchLog.writerLog("送信フォルダ名：" + currentSoshinFolderName, BatchLogLevel.TRACE);

				}

				// 処理：イメージファイルを目的フォルダにコピーする。
				copyFileInSubdirectory(imgFileNameFullPath, currentSoshinFolderName, imgFileName);

				String[] csvnijiOcrData = { batchNo, batchSerialNo, scanReadDate, "0", "0" };
				nijiOcrCsvList.add(csvnijiOcrData);

				batchLog.writerLog("oldSendFolderName：" + oldSoshinFolderName
						+ "  currentSendFolderName：" + currentSoshinFolderName, BatchLogLevel.TRACE);

			} //CSVループ処理終わり

			//最後に読み込んだレコードが残っているので、書き出す。
			if (nijiOcrCsvList.size() > 0) {

				//2次OCR対象ファイル、受信監視ファイル、送信完了ファイル名作成用のTimeStampを作成
				Calendar rightNow = Calendar.getInstance();

				//フォーマットパターンを指定して表示する
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String fileTimeStamp = sdf.format(rightNow.getTime());

				// 処理1:2次OCR対象データをまとめて書き込む
				writeCsvDataToNijiOcrCsvFile(currentSoshinFolderName, nijiOcrCsvList);

				// 処理2:exportsフォルダに空の受信監視ファイルを作成する。
				makeFileJyusinKansiFolder(exportFolderRootPath, currentSoshinFolderName, fileTimeStamp);

				// 処理3:imports\scanフォルダの直下に送信完了ファイルを作成する。
				makeFileSosinKanryoFolder(scanFolderRootPath, currentSoshinFolderName, fileTimeStamp,
						nijiOcrCsvList.size());

				// 処理4:2次OCR対象書き込み用Listを初期化する。
				nijiOcrCsvList.clear();
			}

			// 処理4:2次OCR対象書き込み用Listを初期化する。
			nijiOcrCsvList.clear();

			batchLog.writerLog("JARファイルバージョン：" + constantCommon.JAR_VERSION, BatchLogLevel.INFO);
			batchLog.writerLog("---------バッチ実行正常終了-----------" , BatchLogLevel.INFO);

		} catch (Exception e) {
			e.printStackTrace();
			// TODO 自動生成された catch ブロック
			batchLog.writerLog(e.getMessage(), BatchLogLevel.ERROR);
			batchLog.writerLog("JARファイルバージョン：" + constantCommon.JAR_VERSION, BatchLogLevel.INFO);
			batchLog.writerLog("---------バッチ実行異常終了-----------" , BatchLogLevel.INFO);
		}

		return 0;
	}

	/**
	 * CSV読み取り後、スキップすべきデータを削除する
	 * @param readAllCsvList
	 */
	private void removeSkipCsvData(List<String[]> readAllCsvList) {

		//初期化
		int batchNo = 0;
		int cntSkip = 0;
		String iraisyoSyurui = null;
		String itakuSyacode = null;
		String syoruiSyubetu = null;
		String jyuyoKaCode = null;
		String[] skipItakusyaArray = { "999999", "9999999", "99999999",
				"999999999", "9999999999", "99999999999" };

		// 処理１：全件データを回して削除対象を特定する
		for (String[] csvValueArray : readAllCsvList) {

			// 各列から必要な値を取得して、判断する
			for (int i = 0; i < csvValueArray.length; i++) {

				//必要以上読まない
				if (i > constantCommon.CSV_INDEX_JUYOKACODE)
					break;

				switch (i) {

				//処理１：バッチ番号は９６００以上の場合は、データを削除する。
				case constantCommon.CSV_INDEX_BATCHNO:
					batchNo = Integer.parseInt(csvValueArray[i]);

					if (batchNo >= constantCommon.CSV_LIMIT_BATCHNO) {
						// 後で削除するために、マークをつける
						if (!csvValueArray[0].contains("skip")) {
							csvValueArray[0] = "skip(バッチ番号)_" + csvValueArray[0];
						}
					}
					break;

				//処理２：依頼書種類が０以外だったら、データを削除する。
				case constantCommon.CSV_INDEX_IRAISYOSYURUI:
					iraisyoSyurui = csvValueArray[i];

					if (!iraisyoSyurui.equals(constantCommon.CSV_LIMIT_IRAISYOSYURUI)) {

						// 後で削除するために、マークをつける
						if (!csvValueArray[0].contains("skip")) {
							csvValueArray[0] = "skip(依頼書種類)_" + csvValueArray[0];
						}
					}
					break;

				//処理３：委託者コードがAll 9の場合は、データを削除する。
				case constantCommon.CSV_INDEX_ITAKUSYACODE:
					itakuSyacode = csvValueArray[i];

					//処理：既存のスキップパターンに属するか判断する。
					if (Arrays.asList(skipItakusyaArray).contains(itakuSyacode)) {

						// 後で削除するために、マークをつける
						if (!csvValueArray[0].contains("skip")) {
							csvValueArray[0] = "skip(委託者コード)_" + csvValueArray[0];
						}
					}
					break;

				//処理４：書類種別が１以外の場合は、データを削除する。
				case constantCommon.CSV_INDEX_SYORUISYUBETU:
					syoruiSyubetu = csvValueArray[i];

					if (!syoruiSyubetu.equals(constantCommon.CSV_LIMIT_SYORUISYUBETU)) {

						// 後で削除するために、マークをつける
						if (!csvValueArray[0].contains("skip")) {
							csvValueArray[0] = "skip(書類種別)_" + csvValueArray[0];
						}
					}
					break;

				//処理５：需要家コードが１の場合は、データを削除する。
				case constantCommon.CSV_INDEX_JUYOKACODE:
					jyuyoKaCode = csvValueArray[i];

					if (jyuyoKaCode.equals(constantCommon.CSV_SKIP_JUYOKACODE)) {

						// 後で削除するために、マークをつける
						if (!csvValueArray[0].contains("skip")) {
							csvValueArray[0] = "skip(需要家コード)_" + csvValueArray[0];
						}
					}
					break;

				} //switch終了
			} // 一行のfor終了

		} // 全CSVファイルのfor終了


		// 重要：ConcurrentModificationExceptionエラー発生防止のため、以下のコードを使う
		Iterator<String[]> iter = readAllCsvList.iterator();
		while (iter.hasNext()) {
			String[] csvValueArray = (String[]) iter.next();

			if (csvValueArray[0].startsWith("skip")) {
				iter.remove();
				cntSkip++;
				batchLog.writerLog("スキップ：" + Arrays.toString(csvValueArray), BatchLogLevel.INFO);
			}
		}

		batchLog.writerLog("スキップ件数：" + cntSkip, BatchLogLevel.INFO);

	}

	/**
	 * 2次OCR対象CSVデータを書き込む
	 * @param sousinFolderName: 送信フォルダ名
	 * @param csvData
	 * @throws IOException
	 * 戻り値：書き込んだ後のデータ件数。
	 */
	private int writeCsvDataToNijiOcrCsvFile(String sousinFolderName, List<String[]> csvData) throws IOException {
		//初期化
		String nijiOcrFileName = null;
		int csvDataCnt = 0;
		File file = new File(sousinFolderName);
		List<String[]> nijiOcrCsvList = new ArrayList<String[]>();
		List<String[]> readAllCsvList = new ArrayList<String[]>();
		CSVReader reader = null;

		//2次OCR対象データファイル名を取得する。
		nijiOcrFileName = file.getName();
		nijiOcrFileName += "_snd.csv";

		batchLog.writerLog("--2次OCR対象データ書き込みフォルダ名：" + nijiOcrFileName, BatchLogLevel.TRACE);
		file = new File(sousinFolderName, nijiOcrFileName);
		// 絶対パスに書き換える
		nijiOcrFileName = file.getPath();

		//判断：フォルダが存在しない場合は、作成し、ヘッダを書き込む
		if (!file.exists()) {
			createFile(nijiOcrFileName);
			String[] csvHeader = { "バッチ番号", "バッチ内連番", "スキャナ読取日", "依頼書形式", "回転フラグ" };
			nijiOcrCsvList.add(csvHeader);
		}
		//処理：普段はフォルダが既に存在しているのはありえないが、CSVデータのソートができてない場合を想定
		//して処理を追加
		else {
			//フォルダが存在している場合（イレギュラーケース）
			batchLog.writerLog("★2次OCR既存ファイル名：" + file.getName(), BatchLogLevel.INFO);

			//処理：既存のCSVファイルを読む
			reader = new CSVReader(new FileReader(nijiOcrFileName));

			//既存のCSVファイルを読み取りする。
			readAllCsvList = reader.readAll();

			//処理：既存のデータを追加する。
			for (String[] strData : readAllCsvList) {
				nijiOcrCsvList.add(strData);
			}

		}

		//処理：ヘッダを含む全部のCSVデータを作成する。
		for (String[] strData : csvData) {
			nijiOcrCsvList.add(strData);
		}

		//CSVデータを書き込む
		try {
			CsvUtil.writeCsvData(nijiOcrFileName, nijiOcrCsvList);
			csvDataCnt = nijiOcrCsvList.size() - 1; //ヘッダはカウントしない

		} catch (UnsupportedEncodingException e) {
			// TODO 自動生成された catch ブロック
			batchLog.writerLog(e.getMessage(), BatchLogLevel.ERROR);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			batchLog.writerLog(e.getMessage(), BatchLogLevel.ERROR);
		}

		return csvDataCnt;

	}

	/**
	 * ②exportフォルダに受信監視ファイルを作成（中身は空）
	 * @param rootPath：exportsフォルダの直下
	 * @param sousinFolderName: 送信フォルダ名(フルパスあり)
	 * @param fileTimeStamp：送信フォルダの後ろについて来るタイムスタンプ
	 */
	private void makeFileJyusinKansiFolder(String exportRootPath, String sousinFolderName
			, String fileTimeStamp) {

		//初期化
		String newExportFileName = null;
		String kizonExportFileName = null;

		File fileKizonSousinFolder = new File(sousinFolderName);

		newExportFileName = fileKizonSousinFolder.getName() + "_" + fileTimeStamp;
		newExportFileName = new File(exportRootPath, newExportFileName).getAbsolutePath();

		//既存ファイル名を取得（タイムスタンプあり,パス情報なし）
		kizonExportFileName = exsistsFileExceptionTimeStamp(exportRootPath
									, fileKizonSousinFolder.getName());

		//イレギュラー対応：CSVファイルのソートができてない場合があるので、その場合は
		//exportフォルダにファイルを作成しない。新規のみ作成する
		if (kizonExportFileName == null) {
			createFile(newExportFileName);
		} else {
			//存在しなければ、何もしない
			batchLog.writerLog("★export既存ファイル名：" + kizonExportFileName, BatchLogLevel.INFO);
		}

	}

	/**
	 * ③scanフォルダに送信完了ファイルを作成
	 * @param scanRootPath: scanフォルダの直下
	 * @param sousinFolderName：送信フォルダ名(フルパス)
	 * @param fileTimeStamp：送信フォルダの後ろについて来るタイムスタンプ
	 * @param cnt：送信フォルダにあるJpgファイルの数
	 */
	private void makeFileSosinKanryoFolder(String scanRootPath, String sousinFolderName
			, String fileTimeStamp, int cnt) {

		//初期化
		String newScanfileName = null;
		String kizonScanfileName = null;
		File fileKizonSousinFolder = new File(sousinFolderName);

		newScanfileName = new File(scanRootPath, fileKizonSousinFolder.getName()
				+ "_" + fileTimeStamp + ".end").getAbsolutePath();

		//2次OCR対象データファイル名を取得する。
		kizonScanfileName = exsistsFileExceptionTimeStamp(scanRootPath, fileKizonSousinFolder.getName());
		batchLog.writerLog("★scan新規ファイル名：" + kizonScanfileName, BatchLogLevel.INFO);

		//ファイルのソートが正しくないことを考慮して、存在しない場合のみ、新規作成する
		if (kizonScanfileName == null) {
			createFile(newScanfileName);
			fileKizonSousinFolder = new File(newScanfileName);
		}
		//存在した場合は、既存のファイルを上書きする。
		else {
			fileKizonSousinFolder = new File(scanRootPath, kizonScanfileName);
			batchLog.writerLog("★scan既存ファイル名へ上書き：" + kizonScanfileName, BatchLogLevel.INFO);

		}

		//件数をファイルに書き込む
		try {

			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileKizonSousinFolder), "Shift_JIS");
			BufferedWriter bw = new BufferedWriter(osw);

			bw.write(String.valueOf(cnt));
			bw.newLine();

			bw.close();

		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			batchLog.writerLog(e.getMessage(), BatchLogLevel.ERROR);
		}

	}

	/**送信完了フォルダと受信監視フォルダにファイルが存在しているかチェックする。
	 * @param rootPath
	 * @param exsistFileName
	 * @return 存在したファイル名
	 */
	private String exsistsFileExceptionTimeStamp(String rootPath, String exsistFileName) {

		String kizonFileName = null;
		String kizonFileNameNoTimestamp = null;

		File existFile = null;

		existFile = new File(rootPath);

		// 処理：ルートフォルダのサブディレクトリを検索
		File[] files = existFile.listFiles();
		for (File exsitsFile : files) {
			//処理：タイムスタンプを外す
			kizonFileNameNoTimestamp = exsitsFile.getName().substring(0, 27);
			//			batchLog.writerLog("★kizonFileNameNoTimestamp：" + kizonFileNameNoTimestamp, BatchLogLevel.INFO);

			//処理：フォルダが既に存在しているか確認する
			if (kizonFileNameNoTimestamp.equals(exsistFileName)) {
				kizonFileName = exsitsFile.getName();
				//				batchLog.writerLog("既存ファイル名：" + strExsistsFileName, BatchLogLevel.INFO);
				break;
			}
		}

		return kizonFileName;

	}

	/**
	 *
	 * 送信フォルダ名を取得する。
	 * @param rootPath ルートパス
	 * @param scanReadDate スキャン読み取り日
	 * @param batchNo バッチ番号
	 * @param kaiJi 回次(レコード連番)
	 * @param itakuSyaCode 委託者コード
	 * @return 送信フォルダ名
	 */
	private String getSenderFolderName(String rootPath, String scanReadDate, String batchNo, String kaiJi,
			String itakuSyaCode) {
		String folderName = null;

		folderName = scanReadDate + "_" + batchNo + "_" + kaiJi + "_" + itakuSyaCode;
		folderName = new File(rootPath, folderName).getAbsolutePath();

		return folderName;
	}

	/**
	 * バッチNoの0埋め
	 * @param batchNo
	 * @return
	 */
	private String getFormattedBatchNo(String batchNo) {
		String formattedBatchNo = null;

		formattedBatchNo = String.format("%04d", Integer.parseInt(batchNo));

		return formattedBatchNo;
	}

	/**
	 * レコード連番の左0埋め
	 * @param recordSerialNo
	 * @return
	 */
	private String getFormattedRecordSerialNo(String recordSerialNo) {
		String formattedRecordSerialNo = null;

		formattedRecordSerialNo = String.format("%02d", Integer.parseInt(recordSerialNo));

		return formattedRecordSerialNo;
	}

	/**
	 * バッチ連番の左0埋め
	 * @param batchSerialNo
	 * @return
	 */
	private String getFormattedBatchSerialNo(String batchSerialNo) {
		String formattedBatchSerailNo = null;

		formattedBatchSerailNo = String.format("%03d", Integer.parseInt(batchSerialNo));

		return formattedBatchSerailNo;
	}

	/**
	 * 委託者コードの0埋め
	 * @param itakusyaCode
	 * @return
	 */
	private String getFormattedItakkusyaCode(String itakusyaCode) {
		String formattedStr = null;

		//int範囲を超えるため、Longにする。
		formattedStr = String.format("%010d", Long.parseLong(itakusyaCode));

		return formattedStr;
	}

	/**
	 * プロパティ値を設定ファイルから取得する
	 * @return
	 */
	private propertyCommon getPropertySettingValue() {

		propertyCommon collectionProperty = new propertyCommon();
		ParamUtil propertySetting = new ParamUtil();

		propertySetting.setBatchProp(null);

		collectionProperty.setPropInputFilePath(propertySetting.getInputFilePath());
		collectionProperty.setPropOutputFilePath(propertySetting.getOutFilePath());
		collectionProperty.setPropInfoCsvFileName(propertySetting.getInfoCsvFileName());

		return collectionProperty;

	}

	/**
	 * 出力先のフォルダ構成を作成する
	 * @param propInputRootPath
	 */
	private void initCreateOutputDirectory(String propInputRootPath) {
		String importsPaths[] = { propInputRootPath, constantCommon.DIR_LV1_IMPORTS };
		createDirectory(importsPaths);

		String exportPaths[] = { propInputRootPath, constantCommon.DIR_LV1_EXPORTS };
		createDirectory(exportPaths);

		String scanPaths[] = { propInputRootPath, constantCommon.DIR_LV1_IMPORTS, constantCommon.DIR_LV2_SCAN };
		createDirectory(scanPaths);

		String batchsestPaths[] = { propInputRootPath, constantCommon.DIR_LV1_IMPORTS,
				constantCommon.DIR_LV2_BATCHSETS };
		createDirectory(batchsestPaths);

	}

	/**
	 * ファイルを作成する
	 * @param fileFullName
	 */
	private void createFile(String fileFullName) {

		File newfile = new File(fileFullName);

		try {

			if (newfile.exists()) {
				return;
			}
			newfile.createNewFile();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			batchLog.writerLog(e.getMessage(), BatchLogLevel.ERROR);
		}

	}

	/**
	 * フォルダを作成する
	 * @param String[] args 複数のパスをくっつけてディレクトリを作成する
	 */
	private void createDirectory(String[] args) {

		String folderNameFullPath = "";

		for (int i = 0; i < args.length; i++) {
			folderNameFullPath += args[i] + "\\";
		}

		batchLog.writerLog("ファイルパスは" + folderNameFullPath, BatchLogLevel.TRACE);

		File newFile = new File(folderNameFullPath);

		//判断：フォルダが存在する場合は、終了する。
		if (newFile.exists()) {
			return;

		}

		//ディレクトリを新規作成する
		if (newFile.mkdir()) {
			batchLog.writerLog(folderNameFullPath + "ディレクトリの作成に成功しました。", BatchLogLevel.TRACE);
		} else {
			batchLog.writerLog(folderNameFullPath + "ディレクトリの作成に失敗しました。", BatchLogLevel.ERROR);
		}
	}

	/**
	 * フォルダが存在するかチェックする
	 * @param dirPath
	 * @return
	 */
	private boolean exsistDirectory(String dirPath) {

		File checkDirctory = new File(dirPath);
		//判断：フォルダが存在する場合は、終了する。
		if (checkDirctory.exists()) {
			return true;

		}

		return false;
	}

	/**
	 * @param ファイルを親フォルダから再帰的に検索して、指定フォルダにコピーする
	 */
	private void copyFileInSubdirectory(String sourceImageFileFullPath, String destImageFolder,
			String destImageFileName) {

		File copyFromFile = new File(sourceImageFileFullPath);
		File copyToFile = new File(destImageFolder, destImageFileName);

		try {
			copyFile(copyFromFile, copyToFile);
			batchLog.writerLog("copyFromFile:" + copyFromFile.getPath() + " copyToFile:"
					+ copyToFile.getPath(), BatchLogLevel.INFO);
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			batchLog.writerLog(e.getMessage(), BatchLogLevel.ERROR);
		}

	}

	/**サブディレクトリの直下に画像ファイルが存在するかチェックする。
	 * @param searchRootPath
	 * @param imageFileName
	 * @return
	 */
	private String findFileInSubdirectory(File searchRootPath, String imageFileName) {

		String imageFileFullPath = null;

		//判断：存在しないイメージフォルダの場合は、スキップする。
		if (!exsistDirectory(searchRootPath.getPath())) {
			return imageFileFullPath;

		}

		// 処理：ルートフォルダのサブディレクトリを検索
		File[] files = searchRootPath.listFiles();
		for (File file : files) {
			//001,002などサブディレクトリを検索する
			if (file.isDirectory()) {
				imageFileFullPath = new File(file.getPath(), imageFileName).getPath();
				if (exsistDirectory(imageFileFullPath)) {
					batchLog.writerLog("founded fileName:" + imageFileFullPath, BatchLogLevel.TRACE);
					break;
				}
			}
			imageFileFullPath = null;
		}
		return imageFileFullPath;
	}

	/**
	 * ファイルをコピーする
	 * @param in
	 * @param out
	 * @throws Exception
	 */
	private static void copyFile(File in, File out) throws Exception {
		FileInputStream fis = new FileInputStream(in);
		FileOutputStream fos = new FileOutputStream(out);
		try {
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (fis != null)
				fis.close();
			if (fos != null)
				fos.close();
		}
	}
}
