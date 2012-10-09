package FishFight;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import java.util.*;

public final class Tools {
  public static final int GRAPHCS_TOP_LEFT = Graphics.TOP | Graphics.LEFT;
  private static final Random randomizer = new Random();
  public static int[] Numerics = new int[10];
  public static String dateFormatPattern = "yyyy/MM/dd HH:mm:ss";
  public static String COMMON_ERR_MSG = "错误类型未知，请返回重新订购，或与运营商联系";
  public static String COMMON_SUCCESS_MSG = "订购成功 ";


  // 将数组的长度增加
  /*
   * public final static int[] expandArray(int[] oldArray, int expandBy){ int[]
   * newArray=new int[oldArray.length+expandBy]; System.arraycopy(oldArray,
   * 0,newArray, 0, oldArray.length); return newArray; } public final static
   * Image[][]expandArray(Image[][] oldArray,int expandBy){ Image[][]
   * newArray=new Image[oldArray.length+expandBy][]; System.arraycopy(oldArray,
   * 0, newArray, 0, oldArray.length); return newArray; }
   */
  public final static int getRand(int min, int max) {
    int r = Math.abs(randomizer.nextInt());
    return (r % ((max - min + 1))) + min;
  }

  public final static int setNumerics(int s) {
    if (s == 0) {
      Numerics[0] = 0;
      return 0;
    }
    int t;
    int j = -1;
    while (s > 0) {
      t = s % 10;
      Numerics[++j] = t;
      s = s / 10;
    }
    return j;
  }
  
  public final static int getStringFontWidth(Font font, String str) {
    return font.stringWidth(str);
  }
}
