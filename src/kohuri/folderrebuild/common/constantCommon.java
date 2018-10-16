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

}
