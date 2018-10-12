/**
 *
 */
package kohuri.folderrebuild.util;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * @CSV操作クラス
 *
 */
public class CsvUtil {
	private static final String UTF_8 = "UTF-8";

	/*
	 * CSVファイルを読み取る
	 * */
	public static void readCsv() throws UnsupportedEncodingException, IOException {
		String csv = "abc,def\nhij,klｍ";
		byte[] buf = csv.getBytes(UTF_8);

		InputStream byteIn = new ByteArrayInputStream(buf);
		InputStreamReader in = new InputStreamReader(byteIn, UTF_8);
		CSVReader csvReader = new CSVReader(in);

		List<String[]> lines = csvReader.readAll();
		for (String[] strings : lines) {
			for (String string : strings) {
				System.out.print(string + ",");
			}
			System.out.println("");
		}
		csvReader.close();
	}

	/*
	 * CSV形式で出力する。
	 *
	 * */
	public static void writeCsv() throws UnsupportedEncodingException, IOException {
		List<String[]> csv = Arrays.asList(new String[] { "1", "2" }, new String[] { "3", "4" });

		FileOutputStream out = new FileOutputStream("opencsv.csv");
		Writer writer = new OutputStreamWriter(out);
		CSVWriter csvWriter = new CSVWriter(writer);
		csvWriter.writeAll(csv);

		csvWriter.close();
		writer.close();
		out.close();
	}
}
