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
import java.util.Calendar;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import kohuri.folderrebuild.common.constantCommon;
import kohuri.folderrebuild.common.enumCommon.BatchLogLevel;
import kohuri.folderrebuild.common.propertyCommon;
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

		//2次OCRデータ書き込み用
		List<String[]> nijiOcrCsvList = new ArrayList<String[]>();

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
			String[] nextLine;

			// CSVファイルヘッダは読み飛ばす
			reader.readNext();

			//ループ処理（ここでごちゃごちゃやる感じ）
			// 処理：フォルダ再構築に必要な情報を取得する。
			while ((nextLine = reader.readNext()) != null) {

				//①一行CSVファイルを読んで、必要な値を取得する
				for (int i = 0; i < nextLine.length; i++) {

					//必要以上読まない
					if (i > constantCommon.CSV_INDEX_JPGNAME)
						break;

					switch (i) {
					//バッチ番号
					case constantCommon.CSV_INDEX_BATCHNO:
						batchNo = nextLine[i];
						batchNo = getFormattedBatchNo(batchNo); //左0埋め
						batchLog.writerLog("バッチ番号：" + batchNo, BatchLogLevel.TRACE);
						break;
					//バッチ連番
					case constantCommon.CSV_INDEX_BATCH_SERIALNO:
						batchSerialNo = getFormattedBatchSerialNo(nextLine[i]);
						batchLog.writerLog("バッチ連番：" + batchSerialNo, BatchLogLevel.TRACE);
						break;
					//レコード連番（送信完了ファイルの回次に使用される）
					case constantCommon.CSV_INDEX_RECORD_SERIALNO:
						recordSerialNo = getFormattedRecordSerialNo(nextLine[i]);
						batchLog.writerLog("レコード連番：" + recordSerialNo, BatchLogLevel.TRACE);
						break;
					//スキャナ読取日
					case constantCommon.CSV_INDEX_SCANDATE:
						scanReadDate = nextLine[i];
						batchLog.writerLog("スキャナ読取日：" + scanReadDate, BatchLogLevel.TRACE);
						break;
					//委託者コード
					case constantCommon.CSV_INDEX_ITAKUSYACODE:
						itakuSyaCode = nextLine[i];
						itakuSyaCode = getFormattedItakkusyaCode(itakuSyaCode);
						batchLog.writerLog("委託者コード：" + itakuSyaCode, BatchLogLevel.TRACE);
						break;
					//イメージファイル名
					case constantCommon.CSV_INDEX_JPGNAME:
						imgFileName = nextLine[i];
						batchLog.writerLog("イメージファイル名：" + imgFileName, BatchLogLevel.TRACE);
						break;
					default:

					}
				} // ②ここまで一行読み終わり

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
					writeCsvDataToNijiOcrCsvFile(oldSoshinFolderName, nijiOcrCsvList);

					// 処理2:exportsフォルダに空の受信監視ファイルを作成する。
					makeFileJyusinKansiFolder(exportFolderRootPath, oldSoshinFolderName, fileTimeStamp);

					// 処理3:imports\scanフォルダの直下に送信完了ファイルを作成する。
					makeFileSosinKanryoFolder(scanFolderRootPath, oldSoshinFolderName, fileTimeStamp,
							nijiOcrCsvList.size());

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

				String[] csvData = { batchNo, batchSerialNo, scanReadDate, "0", "0" };
				nijiOcrCsvList.add(csvData);

				batchLog.writerLog("oldSendFolderName：" + oldSoshinFolderName
						+ "  currentSendFolderName：" + currentSoshinFolderName, BatchLogLevel.TRACE);

			} //CSVループ処理終わり

			// 処理4:2次OCR対象書き込み用Listを初期化する。
			nijiOcrCsvList.clear();

		} catch (Exception e) {
			e.printStackTrace();
			// TODO 自動生成された catch ブロック
			batchLog.writerLog(e.getMessage(), BatchLogLevel.ERROR);
		}

		return 0;
	}

	/**
	 * 2次OCR対象CSVデータを書き込む
	 * @param sousinFolderName: 送信フォルダ名
	 * @param csvData
	 */
	private void writeCsvDataToNijiOcrCsvFile(String sousinFolderName, List<String[]> csvData) {
		//初期化
		String nijiOcrFileName = null;
		File file = new File(sousinFolderName);
		List<String[]> nijiOcrCsvList = new ArrayList<String[]>();

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

		//処理：ヘッダを含むCSVデータを作成する。
		for(String[] strData : csvData) {
			nijiOcrCsvList.add(strData);
		}

		//CSVデータを書き込む
		try {
			CsvUtil.writeCsvData(nijiOcrFileName, nijiOcrCsvList);
		} catch (UnsupportedEncodingException e) {
			// TODO 自動生成された catch ブロック
			batchLog.writerLog(e.getMessage(), BatchLogLevel.ERROR);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			batchLog.writerLog(e.getMessage(), BatchLogLevel.ERROR);
		}

	}

	/**
	 * ②exportフォルダに受信監視ファイルを作成（中身は空）
	 * @param rootPath：exportsフォルダの直下
	 * @param sousinFolderName: 送信フォルダ名
	 * @param fileTimeStamp：送信フォルダの後ろについて来るタイムスタンプ
	 */
	private void makeFileJyusinKansiFolder(String rootPath, String sousinFolderName, String fileTimeStamp) {

		//初期化
		String fileName = null;
		File file = null;

		fileName = sousinFolderName + "_" + fileTimeStamp;
		fileName = new File(fileName).getName();
		file = new File(rootPath, fileName);

		//2次OCR対象データファイル名を取得する。
		fileName = file.getPath();
		createFile(fileName);

	}

	/**
	 * ③scanフォルダに送信完了ファイルを作成
	 * @param rootPath: scanフォルダの直下
	 * @param sousinFolderName：送信フォルダ名
	 * @param fileTimeStamp：送信フォルダの後ろについて来るタイムスタンプ
	 * @param cnt：送信フォルダにあるJpgファイルの数
	 */
	private void makeFileSosinKanryoFolder(String rootPath, String sousinFolderName, String fileTimeStamp, int cnt) {
		//初期化
		String fileName = null;
		File file = null;

		fileName = sousinFolderName + "_" + fileTimeStamp + ".end";
		fileName = new File(fileName).getName();
		file = new File(rootPath, fileName);

		//2次OCR対象データファイル名を取得する。
		fileName = file.getPath();
		createFile(fileName);

		//件数をファイルに書き込む
		try {

			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), "Shift_JIS");
			BufferedWriter bw = new BufferedWriter(osw);

			bw.write(String.valueOf(cnt));
			bw.newLine();

			bw.close();

		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			batchLog.writerLog(e.getMessage(), BatchLogLevel.ERROR);
		}

	}

	/**
	 *
	 * 送信フォルダ名を取得する。
	 * @param rootPath ルートパス
	 * @param scanReadDate スキャン読み取り日
	 * @param batchNo バッチ番号
	 * @param kaiJi 回次
	 * @param itakuSyaCode 委託者コード
	 * @return 送信フォルダ名
	 */
	private String getSenderFolderName(String rootPath, String scanReadDate, String batchNo, String kaiJi,
			String itakuSyaCode) {
		String folderName = null;

		folderName = scanReadDate + "_" + batchNo + "_" + kaiJi + "_" + itakuSyaCode;
		folderName = new File(rootPath, folderName).getPath();

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
