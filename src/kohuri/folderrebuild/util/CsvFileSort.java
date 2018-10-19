package kohuri.folderrebuild.util;

/**
 * CSVファイルを読み取り、ソースするためのクラス
 * @author user1
 *
 */
public class CsvFileSort implements Comparable {

	String scanReadDate = null; /*スキャン読み取り日*/
	String batchNo = null; /*バッチ番号*/
	String batchSerialNo = null; /*バッチ連番*/
	String recordSerialNo = null; /*レコード連番*/
	String itakuSyaCode = null; /*委託者コード*/
	String imgFileName = null; /*イメージファイル名（パスなし）*/


	/**
	 * @return scanReadDate
	 */
	public String getScanReadDate() {
		return scanReadDate;
	}
	/**
	 * @param scanReadDate セットする scanReadDate
	 */
	public void setScanReadDate(String scanReadDate) {
		this.scanReadDate = scanReadDate;
	}
	/**
	 * @return batchNo
	 */
	public String getBatchNo() {
		return batchNo;
	}
	/**
	 * @param batchNo セットする batchNo
	 */
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	/**
	 * @return batchSerialNo
	 */
	public String getBatchSerialNo() {
		return batchSerialNo;
	}
	/**
	 * @param batchSerialNo セットする batchSerialNo
	 */
	public void setBatchSerialNo(String batchSerialNo) {
		this.batchSerialNo = batchSerialNo;
	}
	/**
	 * @return recordSerialNo
	 */
	public String getRecordSerialNo() {
		return recordSerialNo;
	}
	/**
	 * @param recordSerialNo セットする recordSerialNo
	 */
	public void setRecordSerialNo(String recordSerialNo) {
		this.recordSerialNo = recordSerialNo;
	}
	/**
	 * @return itakuSyaCode
	 */
	public String getItakuSyaCode() {
		return itakuSyaCode;
	}
	/**
	 * @param itakuSyaCode セットする itakuSyaCode
	 */
	public void setItakuSyaCode(String itakuSyaCode) {
		this.itakuSyaCode = itakuSyaCode;
	}
	/**
	 * @return imgFileName
	 */
	public String getImgFileName() {
		return imgFileName;
	}
	/**
	 * @param imgFileName セットする imgFileName
	 */
	public void setImgFileName(String imgFileName) {
		this.imgFileName = imgFileName;
	}



	/**
	 * @param objCsvFileSort
	 * @return
	 */
	public int compareTo(Object objCsvFileSort) {

		int resualt = 0;

		CsvFileSort csvFileSortOther = (CsvFileSort)objCsvFileSort;

		String strValueCompareSelf = null;
		double doubleValueCompareSelf = (double) -1;
		long compareValueSelf = (long)-1;

		String strValueCompareOther = null;
		double doubleValueCompareOther = (double) -1;
		long compareValueOther = (long)-1;

		//まずはスキャナ読取り日を比較する。
		//①スキャナ読取り日にて既に比較結果がある場合は結果を返す。
		compareValueSelf = Long.parseLong(this.getScanReadDate());
		compareValueOther = Long.parseLong(csvFileSortOther.getScanReadDate());

		resualt = getComapreResualt(compareValueSelf, compareValueSelf);
		if(resualt != 0)  return resualt;


		//②バッチ番号にて既に比較結果がある場合は結果を返す。
		compareValueSelf = Long.parseLong(this.getBatchNo());
		compareValueOther = Long.parseLong(csvFileSortOther.getBatchNo());

		resualt = getComapreResualt(compareValueSelf, compareValueSelf);
		if(resualt != 0)  return resualt;

		//③レコード連番（回次）にて既に比較結果がある場合は結果を返す。
		compareValueSelf = Long.parseLong(this.getRecordSerialNo());
		compareValueOther = Long.parseLong(csvFileSortOther.getRecordSerialNo());

		resualt = getComapreResualt(compareValueSelf, compareValueSelf);
		if(resualt != 0)  return resualt;

		//④委託者コードにて既に比較結果がある場合は結果を返す。
		compareValueSelf = Long.parseLong(this.getItakuSyaCode());
		compareValueOther = Long.parseLong(csvFileSortOther.getItakuSyaCode());

		resualt = getComapreResualt(compareValueSelf, compareValueSelf);
		if(resualt != 0)  return resualt;

		//全部同じの場合は0
		return 0;


	}

	/**
	 * 比較ルーチン
	 * @param self
	 * @param other
	 * @return
	 */
	private int getComapreResualt(long self, long other) {

		if (self < other) {
			return -1;
		} else if (self > other) {
			return 1;
		} else {
			return 0;
		}

	}

}
