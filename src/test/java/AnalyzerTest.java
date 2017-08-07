import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
        String source = "dkdlvhs";

        long start = System.currentTimeMillis();

        System.out.println("Korean analysis init");
        StandardAnalyzer analyzer = new StandardAnalyzer();

        TokenStream stream = analyzer.tokenStream("", new StringReader(source));

        int loop_cnt = 0;
        int loop_cnt2 = 0;
        int end_offset = 0;
        int term_offset = 0;

        Map result_list = new HashMap();
        ArrayList term_morph_list = new ArrayList();

        String origin_text = "";

        OffsetAttribute offSetAttr = null;
        CharTermAttribute termAttr = null;
        PositionIncrementAttribute posAttr = null;
        stream.reset();
        int idx = 0;
        while (stream.incrementToken()) {
            System.out.println("idx:" + idx);
            idx++;
            offSetAttr = (OffsetAttribute) stream.getAttribute(OffsetAttribute.class);
            termAttr = (CharTermAttribute) stream.getAttribute(CharTermAttribute.class);
            posAttr = (PositionIncrementAttribute) stream.getAttribute(PositionIncrementAttribute.class);

            if (end_offset == 0) {
                end_offset = offSetAttr.endOffset();
                term_morph_list = new ArrayList();

                term_offset++;
            }

            System.out.println(termAttr.toString());
            System.out.println("-------------------------------");
            System.out.println(System.currentTimeMillis() - start + "ms");
        }
        stream.end();
        stream.close();
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
