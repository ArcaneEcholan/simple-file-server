package fit.wenchao.http_file_server.utils.name;

import java.util.Random;

/**
 * 英文的姓名，姓氏不很了解。。。目前统一返回简单名称
 */
public class EnglishName {

	private static final EnglishName  single = new EnglishName();

	public static EnglishName getSingle() {
		return single;
	}

	private Random random;
	private int next;
	private EnglishName() {
		random = new Random();
	}
	//public String getFullName() {
	//	return insideName();
	//}

	public String getSimpleName() {
		return insideName();
	}

	public String getIndexName(int index) {
		if(index >= SourceEn.namesEn.length) {
			return null;
		}
		return SourceEn.namesEn[index];
	}

	public static void main(String[] args) {
		System.out.println(getSingle().getSimpleName());
	}

	//public String[] getSplitName() {
	//	return null;
	//}

	private String insideName() {
		next = random.nextInt(SourceEn.namesEn.length);
		return SourceEn.namesEn[next];
	}
}