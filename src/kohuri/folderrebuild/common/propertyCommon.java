package kohuri.folderrebuild.common;

/**
 * プロパティファイルの値を保存するクラス
 * @author user1
 *
 */
public class propertyCommon {

	// 読み取り用ファイルパス
	String propInputFilePath = null;
	// 出力先ルートフォルダパス
	String propOutputFilePath = null;
	// イメージ情報が保存されるCSVファイル
	String propInfoCsvFileName = null;

	/**
	 * @return propInputFilePath
	 */
	public String getPropInputFilePath() {
		return propInputFilePath;
	}

	/**
	 * @param propInputFilePath セットする propInputFilePath
	 */
	public void setPropInputFilePath(String propInputFilePath) {
		this.propInputFilePath = propInputFilePath;
	}

	/**
	 * @return propOutputFilePath
	 */
	public String getPropOutputFilePath() {
		return propOutputFilePath;
	}

	/**
	 * @param propOutputFilePath セットする propOutputFilePath
	 */
	public void setPropOutputFilePath(String propOutputFilePath) {
		this.propOutputFilePath = propOutputFilePath;
	}

	/**
	 * @return propInfoCsvFileName
	 */
	public String getPropInfoCsvFileName() {
		return propInfoCsvFileName;
	}

	/**
	 * @param propInfoCsvFileName セットする propInfoCsvFileName
	 */
	public void setPropInfoCsvFileName(String propInfoCsvFileName) {
		this.propInfoCsvFileName = propInfoCsvFileName;
	}

}
