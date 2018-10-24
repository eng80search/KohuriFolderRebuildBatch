/**
 *
 */
package kohuri.folderrebuild.common;

/**
 * 定数を定義するクラス。
 * @author user1
 *
 */
public class constantCommon {

	// バージョン番号
	public static final String JAR_VERSION = "1.0.1";

	// ルートのディレクトリ名①
	public static final String DIR_LV1_IMPORTS = "imports";

	// importsのサブディレクトリ①
	public static final String DIR_LV2_SCAN = "scan";

	// importsのサブディレクトリ②
	public static final String DIR_LV2_BATCHSETS = "batchsets";

	// ルートのディレクトリ名②
	public static final String DIR_LV1_EXPORTS = "exports";

	// CSVファイル：バッチ番号
	public static final int CSV_INDEX_BATCHNO = 0;

	// CSVファイル：バッチ連番
	public static final int CSV_INDEX_BATCH_SERIALNO = 1;

	// CSVファイル：レコード連番
	public static final int CSV_INDEX_RECORD_SERIALNO = 2;

	// CSVファイル：スキャン読み取り日
	public static final int CSV_INDEX_SCANDATE = 3;

	// CSVファイル：委託者コード
	public static final int CSV_INDEX_ITAKUSYACODE = 14;

	// イメージファイル名
	public static final int CSV_INDEX_JPGNAME = 33;

	// CSVファイル：依頼書種類
	public static final int CSV_INDEX_IRAISYOSYURUI = 7;

	// CSVファイル：書類種別
	public static final int CSV_INDEX_SYORUISYUBETU = 16;

	// CSVファイル：需要家コード
	public static final int CSV_INDEX_JUYOKACODE = 29;

	// CSVファイル：読み取り可能なバッチ番号最大値（含まい）
	public static final int CSV_LIMIT_BATCHNO = 9600;

	// CSVファイル：読み取り可能な依頼書種類
	public static final String CSV_LIMIT_IRAISYOSYURUI = "0";

	// CSVファイル：読み取り可能な書類種別
	public static final String CSV_LIMIT_SYORUISYUBETU = "1";

	// CSVファイル：スキップ可能な委託者コードの正規表現
	public static final String CSV_SKIP_ITAKUSYA_PATTERN = "[9]{1,10}";

	// CSVファイル：読み取り不可な需要家コード
	public static final String CSV_SKIP_JUYOKACODE = "1";


}
