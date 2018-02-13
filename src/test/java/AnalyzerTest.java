import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nobaksan on 2015. 11. 19..
 */
public class AnalyzerTest {

    public static void main(String[] args) throws IOException {

    }

//        public static void main(String[] args) throws Exception {
//        System.out.println("START");
//        JasoDecomposerToken jaso = new JasoDecomposerToken();
//        System.out.println(jaso.runJasoDecompose("아이폰", 0, true));
//        System.out.println("========================================================================================");
//        System.out.println(jaso.runJasoDecompose("iphone", 0, true));
//        System.out.println("========================================================================================");
//        System.out.println(jaso.runJasoDecompose("dkdlvhs", 0, true));
//        System.out.println("========================================================================================");
//        System.out.println(jaso.runJasoDecompose("ㅑㅔㅙㅜㄷ", 0, true));
//        System.out.println("========================================================================================");
//        System.out.println(jaso.runJasoDecompose("ㅎ밈툐", 0, true));
//        System.out.println("========================================================================================");
//        System.out.println(jaso.runJasoDecompose("e편한세상", 0, true));
//        System.out.println(jaso.runJasoDecompose("ㅎ밈툐", 0, true));
//        System.out.println("========================================================================================");
//        System.out.println(jaso.runJasoDecompose("방문ㄷ", 0, true));
//        System.out.println("========================================================================================");
//        System.out.println(jaso.runJasoDecompose("방ㄷ문", 0, true));
//        System.out.println("END");
//    }

}
