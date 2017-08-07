package fastcampus.lucene.example.analyzer;

import java.util.HashMap;

/**
 * Created by kimdongwoo on 15. 4. 1..
 */

public class SoundexConverter {
    static char[] chosungKor = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
    static char[] jungsungKor = {'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'};
    static int[] batchimchosungKor = {1, 2, 4, 7, 0, 8, 16, 17, 0, 19, 20, 21, 22, 0, 23, 24, 25, 26, 27};

    static HashMap<Integer, Integer> choSoundexMap = new HashMap<Integer, Integer>();
    static HashMap<Integer, Integer> jungSoundexMap = new HashMap<Integer, Integer>();

    public SoundexConverter() {
        choSoundexMap.put(15,0);   // ㅋ => ㄱ
        choSoundexMap.put(1,0);    // ㄲ => ㄱ
        choSoundexMap.put(5,2);    // ㄹ => ㄴ
        choSoundexMap.put(4,3);    // ㄸ => ㄷ
        choSoundexMap.put(8,7);    // ㅃ => ㅂ
        choSoundexMap.put(10,9);    // ㅆ => ㅅ
        choSoundexMap.put(14,16);    // ㅊ => ㅌ
        jungSoundexMap.put(2,0);    // ㅑ => ㅏ
        jungSoundexMap.put(20,0);    // ㅣ => ㅏ
        jungSoundexMap.put(6,4);    // ㅕ => ㅓ
        jungSoundexMap.put(12,8);    // ㅛ => ㅗ
        jungSoundexMap.put(17,13);    // ㅠ => ㅜ
        jungSoundexMap.put(18,13);    // ㅡ => ㅜ
        jungSoundexMap.put(7,5);    // ㅖ => ㅔ
        jungSoundexMap.put(1,5);    // ㅐ => ㅔ
        jungSoundexMap.put(3,5);    // ㅒ => ㅔ
    }

    /*
    자소를 분해 하여 soundexing을 실시하고 다시 합친다.
    예제)
    율리얀
    ㅇ ㅠ ㄹ    ㄹ ㅣ  ㅇ ㅑ ㄴ
    ㅇ ㅜ ㄹ    리 ㅣ  ㅇ ㅏ ㄴ
    울리안
     */

    private int[] StringDecompositionToNum(char originChar) {
        int[] resultInt = new int[3];
        int completeCode = originChar;
        int uniValue = completeCode - 44032;
        resultInt[2] = (uniValue % 28);
        resultInt[0] = ((uniValue - resultInt[2]) / 28 / 21);
        resultInt[1] = ((uniValue - resultInt[2]) / 28 % 21);
        return resultInt;
    }

    /*
    오리지널 단어를 가지고 사운덱스화 된 단어로 리턴한다.
     */
    public String getSoundex(String originStr) {
        char[] chars = originStr.toCharArray(); //오리지널 단어를 char화 함

        StringBuilder tokenResult = new StringBuilder(); //결과값을 저장할 공간
        StringBuilder preUmjul = new StringBuilder();   //앞단어의 음절 저장

        int[] jasoResult = new int[3];
        int choJungCode = 0;
        int jongSungCode = 0;
        int isHangulTest = 0;
        int uniCode = 0;


        for (int i = 0; i < chars.length; i++) {    //한 음절씩 처리함.
            if (chars[i] != ' ') {                  //공백이 아니라면
                isHangulTest = chars[i];
                if ((isHangulTest > 44031) && (isHangulTest < 55204)) {   //한글인 경우만 진입
                    jasoResult = StringDecompositionToNum(chars[i]);      //문자를 정수코드로 변환
                    if (jasoResult[0] != 0) {
                        //초성 사운덱스 변환
                        if (choSoundexMap.containsKey(jasoResult[0])) {
                            jasoResult[0] = choSoundexMap.get(jasoResult[0]);
                        }
                    }
                    if (jasoResult[1] != 0) {
                        //중성 사운덱스 변환
                        if (jungSoundexMap.containsKey(jasoResult[1])) {
                            jasoResult[1] = jungSoundexMap.get(jasoResult[1]);
                        }

                    }

                    //중성처리는 당분간 하지 않는다. (이슈 발생시 검토)

                    //변환된 문자 합침
                    uniCode = 44032 + jasoResult[0] * 21 * 28 + jasoResult[1] * 28 + jasoResult[2];
                    Character crChoJungUniCode = new Character((char) uniCode);
                    tokenResult.append(crChoJungUniCode.toString());
                } else {
                    tokenResult.append(chars[i]);
                }
            } else {
                tokenResult.append(chars[i]);
            }
        }
        return tokenResult.toString();
    }

}
