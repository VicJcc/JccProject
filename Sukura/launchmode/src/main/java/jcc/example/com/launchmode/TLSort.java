package jcc.example.com.launchmode;

/**
 * Created by jincancan on 2018/5/16.
 * Description:
 */
public class TLSort {

    public static void fastSort(int[] a, int low, int high){

        if(low >= high){
            return;
        }

        int i = low;
        int j = high;
        int temp = a[low];

        while(i < j){
            while (i < j && a[j] >= temp) {
                j--;
            }
            if(i < j) {
                a[i] = a[j];
                a[j] = temp;
            }

            while (i < j && a[i] < temp) {
                i++;
            }
            if (i < j) {
                a[j] = a[i];
                a[i] = temp;
            }
        }
        fastSort(a, low, i - 1);
        fastSort(a, i + 1 , high);
    }

    public static void sort(int a[], int low, int hight) {
        int i, j, index;
        if (low > hight) {
            return;
        }
        i = low;
        j = hight;
        index = a[i]; // 用子表的第一个记录做基准
        while (i < j) { // 从表的两端交替向中间扫描
            while (i < j && a[j] >= index) {
                j--;
            }
            if (i < j) {
                a[i] = a[j];// 用比基准小的记录替换低位记录
            }
            while (i < j && a[i] < index) {
                i++;
            }
            if (i < j) { // 用比基准大的记录替换高位记录
                a[j] = a[i];
            }
        }
        a[i] = index;// 将基准数值替换回 a[i]
        sort(a, low, i - 1); // 对低子表进行递归排序
        sort(a, i + 1, hight); // 对高子表进行递归排序

    }
}
