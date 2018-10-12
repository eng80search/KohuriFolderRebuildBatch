/**
 *
 */
package kohuri.folderrebuild.util;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import kohuri.folderrebuild.common.enumCommon.BatchLogLevel;

/**
 * ログを出力するクラス
 *
 */
public class LoggerUtil {
	private Logger log = Logger.getLogger(LoggerUtil.class);

	/**
	 * コンストラクタ
	 */
	public LoggerUtil() {
		// 設定ファイルを読み込む
		PropertyConfigurator.configure("./config/log4j.properties");
	}

//	/**
//	 * テスト用メソッド
//	 */
//	/**
//	 *
//	 */
//	public void runSample() {
//
//		// 設定ファイルを読み込む
//		PropertyConfigurator.configure("./config/log4j.properties");
//
//		String a = "テスト";
//
//		log.trace(a); // 出力なし
//		log.debug(a); //2016/07/21 23:19:45.191 [main] DEBUG  テスト
//		log.info(a); //2016/07/21 23:19:45.192 [main] INFO   テスト
//		log.warn(a); //2016/07/21 23:19:45.192 [main] WARN   テスト
//		log.error(a); //2016/07/21 23:19:45.192 [main] ERROR  テスト
//		log.fatal(a); //2016/07/21 23:19:45.192 [main] FATAL  テスト
//	}

	/**
	 * ログレベルに基づいてログを出力する
	 * @param msg
	 * @param level
	 */
	public void writerLog(String msg, BatchLogLevel level) {

		switch (level) {
		case TRACE:
			log.trace(msg);
			break;
		case DEBUG:
			log.debug(msg);
			break;
		case INFO:
			log.info(msg);
			break;
		case WARN:
			log.warn(msg);
			break;
		case ERROR:
			log.error(msg);
			break;
		case FATAL:
			log.fatal(msg);
			break;

		}

	}

}
