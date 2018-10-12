/**
 *
 */
package kohuri.folderrebuild.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * プロパティファイルから値を取得するクラス
 * @author user1
 *
 */
public class ParamUtil {

	// 読み取り元のルートフォルダパス
	private String inputFilePath = null;

	// 出力先のルートフォルダパス
	private String outFilePath = null;

	// 読み取り用CSVファイルのパスと名前
	private String infoCsvFileName = null;

	/**
	 * @return inputFilePath
	 */
	public String getInputFilePath() {
		return inputFilePath;
	}

	/**
	 * @param inputFilePath セットする inputFilePath
	 */
	public void setInputFilePath(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}

	/**
	 * @return outFilePath
	 */
	public String getOutFilePath() {
		return outFilePath;
	}

	/**
	 * @param outFilePath セットする outFilePath
	 */
	public void setOutFilePath(String outFilePath) {
		this.outFilePath = outFilePath;
	}

	/**
	 * @return infoCsvFileName
	 */
	public String getInfoCsvFileName() {
		return infoCsvFileName;
	}

	/**
	 * @param infoCsvFileName セットする infoCsvFileName
	 */
	public void setInfoCsvFileName(String infoCsvFileName) {
		this.infoCsvFileName = infoCsvFileName;
	}

	/**
	 * 設定ファイルからプロパティ値を取得する。
	 * @return
	 */
	private Map<String, String> loadProperty() {

		String propertyFileName = "./config/paramSetting.properties";

		Properties properties = new Properties();

		// プロパティーファイル読み込み
		try {
			InputStream inputStream = new FileInputStream(propertyFileName);
			properties.load(inputStream);
			inputStream.close();
		} catch (IOException e) {
			return null;
		}

		Map<String, String> propMap = new HashMap<String, String>();

		Enumeration<?> en = properties.propertyNames();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			String value = properties.getProperty(key);
			if (value == null || value.isEmpty()) {
				value = null;

			}
			propMap.put(key, value);

		}

		return propMap;
	}


	/**
	 * プロパティファイルからの設定値を取得し、プロパティに格納する。
	 * @param args
	 * @return
	 */
	public boolean setBatchProp(String[] args) {

		boolean result = true;

		// プロパティーファイルの値の取得とチェック
		Map<String, String> propMap = loadProperty();
		if (propMap == null) {
			return false;
		}


		// 読み取り元のルートフォルダパスを取得する。
		String tmpInputFilePath = propMap.get("property.inputFilePath");
		if (tmpInputFilePath == null) {
			result = false;
		}

        setInputFilePath(tmpInputFilePath);


		// 出力先のルートフォルダパスを取得する。
		String tmpOutFilePath = propMap.get("property.outFilePath");
		if (tmpOutFilePath == null) {
			result = false;
		}

        setOutFilePath(tmpOutFilePath);

		// 読み取り用CSVファイル名を取得する。
		String tmpInfoCsvFile = propMap.get("property.infoCsvFileName");
		if (tmpInfoCsvFile == null) {
			result = false;
		}

        setInfoCsvFileName(tmpInfoCsvFile);

		return result;
	}

}
