package fastcampus.lucene.example.analyzer;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

/**
 * 쿼리젯 김지훈
 */
public class SoundexTokenFilter extends TokenFilter {
    private int decomposeMode = 0;
    private boolean typoMode = true;

    public SoundexTokenFilter(TokenStream tokenStream) {
        super(tokenStream);
        this.charTermAttr = addAttribute(CharTermAttribute.class);
        this.posIncAttr = addAttribute(PositionIncrementAttribute.class);
    }
    private CharTermAttribute charTermAttr;
    private PositionIncrementAttribute posIncAttr;

    @Override
    public boolean incrementToken() throws IOException {

        if (!input.incrementToken()) {
            return false;
        } else {
            SoundexConverter soundexConverter = new SoundexConverter();
            String currentTokenInStream = this.input.getAttribute(CharTermAttribute.class).toString().trim(); //현재 토큰값을 받아온다
            String decomposerTokenInStream =  soundexConverter.getSoundex(currentTokenInStream); //사운덱스 값으로 변환한다
            charTermAttr.setEmpty();                        //현재 텀을 지우고
            charTermAttr.append(decomposerTokenInStream);   //새로운 텀을 추가함
            return true;
        }
    }

}
