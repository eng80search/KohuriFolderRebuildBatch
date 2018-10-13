package kohuri.folderrebuild.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import kohuri.folderrebuild.common.constantCommon;
import kohuri.folderrebuild.common.enumCommon.BatchLogLevel;
import kohuri.folderrebuild.common.propertyCommon;
import kohuri.folderrebuild.util.LoggerUtil;
import kohuri.folderrebuild.util.ParamUtil;;

/*
 * メイン処理クラス
 * CSVファイルからフォルダ再構築情報を読み取り、設定ファイルからの情報に
 * 基づいてフォルダの再構築処理を行う。
*/
/**
 * @author user1
 *
 */
public class FolderRebuild {
	// ログ用
	static LoggerUtil batchLog = new LoggerUtil();
	// 再帰的にファイルを探してコピーするためのフラグ
	private static boolean foundFile = false;

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

		//		// ログ用
		//		LoggerUtil batchLog = new LoggerUtil();



		// 初期化：sendFolderName batchsets直下の送信フォルダ名=Empty
		String scanFolderRootPath = null; /*scanフォルダルートパス*/
		String exportFolderRootPath = null; /*exportフォルダルートパス*/
		String sendFolderRootPath = null; /*送信フォルダルートパス*/
		String currentSendFolderName = ""; /*送信フォルダ名*/
		String oldSendFolderName = ""; /*送信フォルダ名*/
		String scanReadDate = null; /*スキャン読み取り日*/
		String batchNo = null; /*バッチ番号*/
		String batchSerialNo = null; /*バッチ連番*/
		String itakuSyaCode = null; /*委託者コード*/
		String imgFileName = null; /*イメージファイル名（パスなし）*/
		String nijiOcrCsvFileName = null; /*二次OCR対象ファイル名*/

		//2次OCRデータ書き込み用
		String[] nijiOcrCsvArray = new String[5];
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
			batchLog.writerLog(propInputRootPath, BatchLogLevel.TRACE);
			batchLog.writerLog(propOutputRootPath, BatchLogLevel.TRACE);
			batchLog.writerLog(propInfoCsvFileName, BatchLogLevel.TRACE);

			// ②出力先にフォルダ階層を構築する。
			initCreateOutputDirectory(propOutputRootPath);
			scanFolderRootPath = propOutputRootPath + "\\"+ constantCommon.DIR_LV1_IMPORTS
					+ "\\" + constantCommon.DIR_LV2_SCAN;
			sendFolderRootPath = propOutputRootPath + "\\"+ constantCommon.DIR_LV1_IMPORTS
					+ "\\" + constantCommon.DIR_LV2_BATCHSETS;
			exportFolderRootPath = propOutputRootPath + "\\"+ constantCommon.DIR_LV1_EXPORTS;


			// ③Loop処理:CSVファイルを一行読み取る。(レコードが何万件なので、仕方なくここに書く)

			CSVReader reader = new CSVReader(new FileReader(propInfoCsvFileName));
			String[] nextLine;

			// CSVファイルヘッダは読み飛ばす
			reader.readNext();

			//ループ処理（ここでごちゃごちゃやる感じ）
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
					case constantCommon.CSV_INDEX_SERIALNO:
						batchSerialNo = nextLine[i];
						batchLog.writerLog("バッチ連番：" + batchSerialNo, BatchLogLevel.TRACE);
						break;
					//スキャナ読取日
					case constantCommon.CSV_INDEX_SCANDATE:
						scanReadDate = nextLine[i];
						batchLog.writerLog("スキャナ読取日：" + scanReadDate, BatchLogLevel.TRACE);
						break;
					//委託者コード
					case constantCommon.CSV_INDEX_ITAKUSYACODE:
						itakuSyaCode = nextLine[i];
						batchLog.writerLog("委託者コード：" + itakuSyaCode, BatchLogLevel.TRACE);
						break;
					//イメージファイル名
					case constantCommon.CSV_INDEX_JPGNAME:
						imgFileName = nextLine[i];
						batchLog.writerLog("イメージファイル名：" + imgFileName, BatchLogLevel.TRACE);
						break;
					default:

					}
				}

				// ②ここまで一行読み終わり

				// 31.取込元イメージ名及び出力先フォルダ名を取得する。（済）
				// 32.取り込み元フォルダからファイルを特定する。（済）

				// 処理１：前のフォルダ情報を退避する。
				oldSendFolderName = new String(currentSendFolderName.toString());

				// 33.送信フォルダ名を取得する。
				currentSendFolderName = getSenderFolderName(sendFolderRootPath, scanReadDate, batchNo, "00",
						itakuSyaCode);
				batchLog.writerLog("送信フォルダ名：" + currentSendFolderName, BatchLogLevel.TRACE);

				// 34.判断：batchestsの直下に送信フォルダサブディレクトリが存在するか
				if (!exsistDirectory(currentSendFolderName)) {
					// 存在しない場合:

					// 処理１：新規フォルダを作成する。
					String newFolder[] = { currentSendFolderName };
					createDirectory(newFolder);
					// 処理２：2次OCR対象ファイル作成
					nijiOcrCsvFileName = makeNijiOcrCsvFile(currentSendFolderName);

				}


				//処理：イメージファイルをbatchsets送信フォルダにコピーする(上書き方式)
				File sourceImageDirectory = new File(propInputRootPath + "\\" + scanReadDate);
				copyFileRecursively(sourceImageDirectory,currentSendFolderName,imgFileName);
				//判断：コピー元ファイルが存在した場合
				if(foundFile)
				{
					//書き込み用データを退避する。
					//処理1:前回の対象フォルダの直下に2次OCR対象ファイルを作成する。
					nijiOcrCsvArray[0] = batchNo; //バッチ番号
					nijiOcrCsvArray[1] = batchSerialNo; //バッチ内連番
					nijiOcrCsvArray[2] = scanReadDate; //スキャナ読取り日
					nijiOcrCsvArray[3] = "0"; //依頼書形式０で固定
					nijiOcrCsvArray[4] = "0"; //回転フラグ

					nijiOcrCsvList.add(nijiOcrCsvArray);

					//コピーフラグをリセットする
					foundFile = false;
				}


				// 判断A：結果CSVファイルを作成すべきかどうか
				// 判断基準：前回退避した送信フォルダがnullでない且つ今回の送信フォルダ名と
				//			 違うときである。
				if (oldSendFolderName != "" && !oldSendFolderName.equals(currentSendFolderName)) {

					//2次OCR対象ファイル、受信監視ファイル、送信完了ファイル名作成用のTimeStampを作成
					Calendar  rightNow = Calendar.getInstance();

			        //フォーマットパターンを指定して表示する
			        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
					String fileTimeStamp = sdf.format(rightNow.getTime());

					// 処理1:2次OCR対象データをまとめて書き込む
					writeCsvDataToNijiOcrCsvFile(oldSendFolderName, fileTimeStamp, nijiOcrCsvList);

					// 処理2:exportsフォルダに空の受信監視ファイルを作成する。
					makeFileJyusinKansiFolder(exportFolderRootPath, oldSendFolderName,fileTimeStamp);

					// 処理3:imports\scanフォルダの直下に送信完了ファイルを作成する。
					makeFileSosinKanryoFolder(scanFolderRootPath, oldSendFolderName,fileTimeStamp, 0);

					// 処理4:2次OCR対象書き込み用Listを初期化する。
					nijiOcrCsvList.clear();

				}


			}

		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			batchLog.writerLog(e.getMessage(), BatchLogLevel.ERROR);
		}

		return 0;
	}



	/**
	 * 2次OCR対象CSVデータを書き込む
	 * @param sousinFolderName: 送信フォルダ名
	 * @param fileTimeStamp：送信フォルダの後ろについて来るタイムスタンプ
	 * @param csvData
	 */
	private void writeCsvDataToNijiOcrCsvFile(String sousinFolderName, String fileTimeStamp, List<String[]> csvData)
	{
		//初期化
		String nijiOcrFileName = null;

		batchLog.writerLog("2次OCR対象データ書き込み：" + nijiOcrFileName, BatchLogLevel.TRACE);


		//2次OCR対象データファイル名を取得する。


		//CSVデータを書き込む

	}

	/**
	 * 2次OCR対象ファイルを作成し、ファイル名を返す
	 * @param senderFolderName：送信フォルダルートパス
	 */
	private String makeNijiOcrCsvFile(String senderFolderName)
	{
		//初期化
		String nijiOcrFileName = null;
		File file = new File(senderFolderName);

		//2次OCR対象データファイル名を取得する。
		nijiOcrFileName = file.getName();
		nijiOcrFileName += "_snd.csv";

		nijiOcrFileName = file.getPath() + "\\" + nijiOcrFileName;
		createFile(nijiOcrFileName);

		batchLog.writerLog("2次OCR対象データファイル名：" + nijiOcrFileName, BatchLogLevel.TRACE);

		return nijiOcrFileName;


	}


	/**
	 * ②exportフォルダに受信監視ファイルを作成（中身は空）
	 * @param rootPath：exportsフォルダの直下
	 * @param sousinFolderName: 送信フォルダ名
	 * @param fileTimeStamp：送信フォルダの後ろについて来るタイムスタンプ
	 */
	private void makeFileJyusinKansiFolder(String rootPath, String sousinFolderName, String fileTimeStamp)
	{


	}



	/**
	 * ③scanフォルダに送信完了ファイルを作成
	 * @param rootPath: scanフォルダの直下
	 * @param sousinFolderName：送信フォルダ名
	 * @param fileTimeStamp：送信フォルダの後ろについて来るタイムスタンプ
	 * @param cnt：送信フォルダにあるJpgファイルの数
	 */
	private void makeFileSosinKanryoFolder(String rootPath, String sousinFolderName, String fileTimeStamp, int cnt)
	{

	}

	/**
	 *
	 * 送信フォルダ名を取得する。
	 * @param scanReadDate スキャン読み取り日
	 * @param batchNo バッチ番号
	 * @param kaiJi 回次
	 * @param itakuSyaCode 委託者コード
	 * @return 送信フォルダ名
	 */
	private String getSenderFolderName(String rootPath, String scanReadDate, String batchNo, String kaiJi, String itakuSyaCode) {
		String folderName = null;

		folderName = scanReadDate + "_" + batchNo + "_" + kaiJi + "_" + itakuSyaCode;
		folderName = new File(rootPath,folderName).getPath();

		return folderName;
	}

	/**
	 * バッチNoの0埋め
	 * @param batchNo
	 * @return
	 */
	private String getFormattedBatchNo(String batchNo)
	{
		String formattedBatchNo = null;

		formattedBatchNo = String.format("%04d", Integer.parseInt(batchNo));

		return formattedBatchNo;
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

		String scanPaths[] = { propInputRootPath,constantCommon.DIR_LV1_IMPORTS, constantCommon.DIR_LV2_SCAN};
		createDirectory(scanPaths);

		String batchsestPaths[] = { propInputRootPath,constantCommon.DIR_LV1_IMPORTS, constantCommon.DIR_LV2_BATCHSETS};
		createDirectory(batchsestPaths);

		String batchPaths[] = { propInputRootPath,constantCommon.DIR_LV1_IMPORTS, constantCommon.DIR_LV1_EXPORTS };
		createDirectory(batchPaths);
	}


	/**
	 * ファイルを作成する
	 * @param fileFullName
	 */
	private void createFile(String fileFullName) {

		File newfile = new File(fileFullName);
		try {
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

		for (int i = 0; i < args.length ; i++) {
			folderNameFullPath += args[i] + "\\";
		}

		batchLog.writerLog("ファイルパスは" + folderNameFullPath , BatchLogLevel.TRACE);

		File newFile = new File(folderNameFullPath);

		//判断：フォルダが存在する場合は、終了する。
		if (newFile.exists()) {
			return;

		}

		//ディレクトリを新規作成する
		if (newFile.mkdir()) {
			batchLog.writerLog(folderNameFullPath + "ディレクトリの作成に成功しました。", BatchLogLevel.INFO);
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
	private static void copyFileRecursively(File copyFromDir, String copyToDir, String imageFileName) {
		if (foundFile) {
			return;
		}
		File[] files = copyFromDir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				copyFileRecursively(file,copyToDir, imageFileName);
			}
			if (file.getName().equals(imageFileName)) {
				File copyFromFile = file;
				File copyToFile = new File(copyToDir, imageFileName);

				try {
					copyFile(copyFromFile, copyToFile);
					foundFile = true;
					batchLog.writerLog(copyFromFile + "を" + copyToFile + "にコピーしました。", BatchLogLevel.DEBUG);
				} catch (Exception e) {
					// TODO 自動生成された catch ブロック
//					e.printStackTrace();
					batchLog.writerLog(e.getMessage(), BatchLogLevel.ERROR);
				}
				break;
			}

		}
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
