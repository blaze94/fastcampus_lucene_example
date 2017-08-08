package fastcampus.lucene.example.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/*
예제:
Ram디렉토리에
간단한 데이터를 색인 후
검색을 실행해 보는 예제


 */
public class AnalyzerTest {
    public static void main(String[] args) throws IOException, ParseException {
        long start = System.currentTimeMillis();

        String testStr= "삼성전자";

        Analyzer myKeywordAnalyzer = new TestAnalyzer();
        TokenStream stream = myKeywordAnalyzer.tokenStream("", new StringReader(testStr));



        int idx = 0;
        int end_offset = 0;
        int term_offset = 0;
        ArrayList term_morph_list = new ArrayList();
        stream.reset();

        while (stream.incrementToken()) {
            System.out.println("idx:" + idx);
            idx++;
            OffsetAttribute offSetAttr = null;
            CharTermAttribute termAttr = null;
            PositionIncrementAttribute posAttr = null;

            offSetAttr = (OffsetAttribute) stream.getAttribute(OffsetAttribute.class);
            termAttr = (CharTermAttribute) stream.getAttribute(CharTermAttribute.class);
            posAttr = (PositionIncrementAttribute) stream.getAttribute(PositionIncrementAttribute.class);

            if (end_offset == 0) {
                end_offset = offSetAttr.endOffset();
                term_morph_list = new ArrayList();
                term_offset++;
            }

            System.out.println(termAttr.toString());
            System.out.println(offSetAttr.startOffset() + "~" + offSetAttr.endOffset());
            System.out.println(posAttr.getPositionIncrement());

            System.out.println("-------------------------------");

            System.out.println(System.currentTimeMillis() - start + "ms");
        }
        stream.end();
        stream.close();
    }

}