package fastcampus.lucene.example.search;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.icu.ICUFoldingFilter;
import org.apache.lucene.analysis.icu.ICUNormalizer2Filter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/*
  #A 스펠체커용 인덱스 생성
  #B 편집거리 계산
  #C 후보군 생성
*/
public class SpellCheckerExample {


  public static void main(String[] args) throws Exception {
    Directory directory = FSDirectory.open(Paths.get("./index/spell/"));
    SpellChecker spellChecker = new SpellChecker(directory);

    Analyzer analyzer = new StandardAnalyzer();                             //기본 스탠다드분석기를 사용함
//    Analyzer analyzer = new Analyzer() {
//      @Override
//      protected TokenStreamComponents createComponents(String s) {
//        Reader reader = new StringReader(s);
//        Tokenizer tokenizer = new StandardTokenizer();
//        tokenizer.setReader(reader);
//        String name = "nfc_cf";
//        Normalizer2 normalizer =  Normalizer2.getInstance(null, name, Normalizer2.Mode.DECOMPOSE);
//        TokenFilter filter = new ICUNormalizer2Filter(tokenizer, normalizer);
//        return new TokenStreamComponents(tokenizer, filter);
//      }
//    };

    IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);  //인덱스 Writer의 설정을 지정하는 클래스
    Path path = Paths.get("./data/spell/dic.txt");

    spellChecker.setSpellIndex(directory);
    spellChecker.clearIndex();
    spellChecker.indexDictionary(
            new PlainTextDictionary(path),indexWriterConfig,true);
    String wordForSuggestions = "이승찰";
    //spellChecker.setStringDistance(new LevensteinDistance());  //#Levenstein 편집거리 알고리즘
    spellChecker.setStringDistance(new JaroWinklerDistance());  //Jaro-Winkler 알고리즘

    int suggestionsNumber = 1;
    String[] suggestions =
            spellChecker.suggestSimilar(wordForSuggestions, suggestionsNumber);
    if (suggestions != null && suggestions.length > 0) {

      for (String word : suggestions) {

        System.out.println("Did you mean:" + word );


      }

    } else {

      System.out.println("No suggestions found for word:" + wordForSuggestions);

    }


  }

}


