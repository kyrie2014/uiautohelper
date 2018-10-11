package com.unisoc.bmte.uiautohelper;

import com.google.zxing.Result;

import org.junit.Test;

import static com.unisoc.bmte.uiautohelper.Common.print;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        Result [] results = QrCodePhotoModule.scanningImage("/sdcard/DCIM/Camera/IMG_20180912_122423.JPG");
        if (results!=null){
            print(results.toString());
        }
//        assertEquals(4, 2 + 2);
    }
}