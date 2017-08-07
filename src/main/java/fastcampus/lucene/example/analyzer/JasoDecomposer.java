package fastcampus.lucene.example.analyzer;

import java.util.regex.Pattern;

/**
 * Created by kimdongwoo on 15. 4. 1..
 */

public class JasoDecomposer {
    static String[] chosungEng = {"r", "R", "s", "e", "E", "f", "a", "q", "Q", "t", "T", "d", "w", "W", "c", "z", "x", "v", "g"};
    static String[] jungsungEng = {"k", "o", "i", "O", "j", "p", "u", "P", "h", "hk", "ho", "hl", "y", "n", "nj", "np", "nl", "b", "m", "ml", "l"};
    static String[] jongsungEng = {" ", "r", "R", "rt", "s", "sw", "sg", "e", "f", "fr", "fa", "fq", "ft", "fx", "fv", "fg", "a", "q", "qt", "t", "T", "d", "w", "c", "z", "x", "v", "g"};
    static String[] singleJaumEng = { "r", "R", "rt", "s", "sw", "sg", "e","E" ,"f", "fr", "fa", "fq", "ft", "fx", "fv", "fg", "a", "q","Q", "qt", "t", "T", "d", "w", "W", "c", "z", "x", "v", "g" };

    static char[] chosungKor = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};

    static int[] batchimchosungKor = {1, 2, 4, 7, 0, 8, 16, 17, 0, 19, 20, 21, 22, 0, 23, 24, 25, 26, 27};

    /**
     * 음절 => 유니코드 배열로
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


    /**
    * 한글 범위인가?
     */
    public boolean IsHangul(String originStr) {
        int imsi = 0;
        char[] chars = originStr.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != ' ') {
                imsi = chars[i];

                if ((imsi > 44031) && (imsi < 55204)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String decomposer(String originStr) {
        char[] chars = originStr.toCharArray();

        StringBuilder tokenResult = new StringBuilder();
        StringBuilder preUmjul = new StringBuilder();

        int[] jasoResult = new int[3];
        int choJungCode = 0;
        int jongSungCode = 0;
        int isHangulTest = 0;
        int uniCode = 0;

        for (int i = 0; i < chars.length; i++) {    //한음절씩 처리
            if (chars[i] != ' ') {
                isHangulTest = chars[i];
                if ((isHangulTest > 44031) && (isHangulTest < 55204)) {     //한글 범위 내만 처리

                    jasoResult = StringDecompositionToNum(chars[i]);
                    char chosungImsi = chosungKor[jasoResult[0]];
                    Character crChosungImsi = new Character(chosungImsi);
                    tokenResult.append(preUmjul + crChosungImsi.toString() + "★");

                    if (choJungCode != 0) {
                        boolean multiBatchim = false;
                        if (jongSungCode != 0) {
                            switch (jongSungCode) {
                                case 1:
                                    if (jasoResult[0] == 9) {
                                        choJungCode += 3;
                                        multiBatchim = true;
                                    }
                                    break;
                                case 4:
                                    if (jasoResult[0] == 12) {
                                        choJungCode += 5;
                                        multiBatchim = true;
                                    } else if (jasoResult[0] == 18) {
                                        choJungCode += 6;
                                        multiBatchim = true;
                                    }
                                    break;
                                case 8:
                                    if (jasoResult[0] == 0) {
                                        choJungCode += 9;
                                        multiBatchim = true;
                                    } else if (jasoResult[0] == 6) {
                                        choJungCode += 10;
                                        multiBatchim = true;
                                    } else if (jasoResult[0] == 7) {
                                        choJungCode += 11;
                                        multiBatchim = true;
                                    } else if (jasoResult[0] == 9) {
                                        choJungCode += 12;
                                        multiBatchim = true;
                                    } else if (jasoResult[0] == 16) {
                                        choJungCode += 13;
                                        multiBatchim = true;
                                    } else if (jasoResult[0] == 17) {
                                        choJungCode += 14;
                                        multiBatchim = true;
                                    } else if (jasoResult[0] == 18) {
                                        choJungCode += 15;
                                        multiBatchim = true;
                                    }
                                    break;
                                case 17:
                                    if (jasoResult[0] == 9) {
                                        choJungCode += 18;
                                        multiBatchim = true;
                                    }
                                    break;
                            }
                            if (multiBatchim) {
                                char addChoJungCode = (char) choJungCode;
                                Character crAddChoJungCode = new Character(addChoJungCode);
                                tokenResult.append(preUmjul.toString().substring(0, preUmjul.length() - 1) + crAddChoJungCode.toString() + "★");
                            }

                        } else {
                            choJungCode += batchimchosungKor[jasoResult[0]];
                            char choJungCodeImsi = (char) choJungCode;
                            Character crChoJungCodeImsi = new Character(choJungCodeImsi);
                            tokenResult.append(preUmjul.toString().substring(0, preUmjul.length() - 1) + crChoJungCodeImsi.toString() + "★");
                        }

                    }

                    uniCode = 44032 + jasoResult[0] * 21 * 28 + jasoResult[1] * 28 + 0;
                    Character crChoJungUniCode = new Character((char) uniCode);
                    tokenResult.append(preUmjul + crChoJungUniCode.toString() + "★");

                    if (jasoResult[2] != 0) {
                        uniCode = 44032 + jasoResult[0] * 21 * 28 + jasoResult[1] * 28 + jasoResult[2];
                        Character crChoJungJongUniCode = new Character((char) uniCode);
                        tokenResult.append(preUmjul + crChoJungJongUniCode.toString() + "★");

                        if ((jasoResult[2] == 1) || (jasoResult[2] == 4) || (jasoResult[2] == 8) || (jasoResult[2] == 17)) {
                            choJungCode = 44032 + jasoResult[0] * 21 * 28 + jasoResult[1] * 28;
                            jongSungCode = jasoResult[2];
                        } else {
                            choJungCode = 0;
                            jongSungCode = 0;
                        }
                    } else {
                        choJungCode = 44032 + jasoResult[0] * 21 * 28 + jasoResult[1] * 28;
                        jongSungCode = 0;
                    }

                    Character crPreUmjul = new Character((char) uniCode);
                    preUmjul.append(crPreUmjul.toString());

                }
            } else {
                preUmjul.append(" ");
            }
        }

        return tokenResult.toString();
    }
}
