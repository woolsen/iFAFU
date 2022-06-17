package cn.ifafu.ifafu.util;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

public class VerifyParser {

    private              double[][] weight;
    private static final File       data = new File("/Users/woolsen/StudioProjects/iFAFU-Android/app/src/main/assets/theta.dat");

    public void init() {
        try (InputStream inputStream = new FileInputStream(data)) {
            weight = new double[34][337];
            Scanner scanner = new Scanner(inputStream);
            for (int i = 0; i < 34; i++) {
                for (int j = 0; j < 337; j++) {
                    weight[i][j] = scanner.nextDouble();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String todo(Bitmap bitmap) {
        if (weight == null) {
            init();
        }
        if (bitmap == null) {
            return "";
        }
        long longStart = System.currentTimeMillis();
        int[][] data = prepareData(bitmap);

        for (int[] datum : data) {
            for (int i : datum) {
                System.out.println(i + ' ');
            }
            System.out.println();
        }

        /* transpose data to x for linear classifier */
        double[][] x = new double[4][337];
        for (int i = 0; i < 4; i++) {
            x[i][0] = 1;
            for (int j = 1; j < 337; j++) {
                x[i][j] = 1.0 * data[i][j - 1] / 255;
            }
        }

        /* predict */
        double[][] y = dot(weight, x);
        double[][] p = sigmoid(y);

        /* classify */
        char[] chr = new char[4];
        for (int i = 0; i < 4; i++) {
            double max = 0;
            int clas = 0;

            for (int j = 0; j < 34; j++) {
                if (p[i][j] > max) {
                    max = p[i][j];
                    clas = j;
                }
            }

            chr[i] = (char) (clas <= 9 ? clas + 48 : (clas <= 23 ? clas + 87 : clas + 88));
        }

        long time = System.currentTimeMillis() - longStart;
        Timber.d("Classify verify code use time: %ds%dms", time / 1000, time % 1000);
        return String.valueOf(chr);
    }

    private double[][] sigmoid(double[][] y) {
        double[][] answer = new double[4][34];

        for (int i = 0; i < y.length; i++) {
            for (int j = 0; j < y[i].length; j++) {
                //  Sigmoid
                answer[i][j] = 1.0 / (1.0 + Math.exp(-1.0 * y[i][j]));
            }
        }

        return answer;
    }

    private double[][] dot(double[][] weight, double[][] x) {
        double[][] answer = new double[4][34];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 34; j++) {
                double t = 0;
                for (int k = 0; k < 337; k++) {
                    t = t + (x[i][k] * (weight[j][k]));
                }
                answer[i][j] = t;
            }
        }

        return answer;
    }

    private int[][] prepareData(Bitmap bitmap) {
        int xSize = bitmap.getWidth();
        int ySize = bitmap.getHeight() - 5;
        int piece = (xSize - 22) / 8;

        int[] centers = {0, 0, 0, 0};
        for (int i = 0; i < 4; i++) {
            centers[i] = 4 + piece * (2 * i + 1);
        }

        int[][] matrix = new int[4][(2 * piece + 4) * (ySize - 1)];
        for (int k = 0; k < 4; k++) {
            int center = centers[k];
            int ii = 0;
            for (int j = 1; j < ySize; j++) {
                for (int i = center - (piece + 2); i < center + (piece + 2); i++) {
                    matrix[k][ii++] = convertGreyDegree(bitmap.getPixel(i, j));
                }
            }
        }

        return matrix;
    }

    private int convertGreyDegree(int argb) {
        int red = (argb >> 16) & 0xff;
        int green = (argb >> 8) & 0xff;
        int blue = argb & 0xff;

        return (red * 30 + green * 59 + blue * 11 + 50) / 100;
    }

    public String parse(Response<ResponseBody> response) throws IOException {
        if (response.code() == 302) {
            throw new IOException();
        }
        return todo(BitmapUtil.bytesToBitmap(response.body().bytes()));
    }

    public String parse2(ResponseBody body) throws IOException {
        return todo(BitmapUtil.bytesToBitmap(body.bytes()));
    }
}
