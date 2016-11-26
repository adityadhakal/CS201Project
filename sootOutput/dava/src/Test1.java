import java.io.PrintStream;

public class Test1
{
    static long _1_0;
    static long _2_0;
    static long _2_1;
    static long _2_2;
    static long _2_3;
    static long _2_4;
    static long _2_5;
    static long _2_6;
    static long _2_7;
    static long _3_0;

    public static void main(String[]  r0)
    {

        byte b0, b1;
        PrintStream r1, r2, r3, r4, r5, r6, r7, r8, r9, r10;
        long l2, l3, l4, l5, l6, l7, l8, l9, l10, l11, l12, l13, l14, l15;
        b0 = (byte) (byte) 0;
        b1 = (byte) (byte) 95;
        Test1.func1(b0);
        Test1.func1(b1);
        EdgeProfiling.increase(0, 5, 5);
        l2 = _1_0;
        l4 = l2 + 1L;
        _1_0 = l4;
        r1 = System.out;
        l3 = _1_0;
        r1.println(l3);
        l5 = _1_0;
        l6 = l5 + 1L;
        _1_0 = l6;
        r2 = System.out;
        l7 = _2_0;
        r2.println(l7);
        r3 = System.out;
        l8 = _2_1;
        r3.println(l8);
        r4 = System.out;
        l9 = _2_2;
        r4.println(l9);
        r5 = System.out;
        l10 = _2_3;
        r5.println(l10);
        r6 = System.out;
        l11 = _2_4;
        r6.println(l11);
        r7 = System.out;
        l12 = _2_5;
        r7.println(l12);
        r8 = System.out;
        l13 = _2_6;
        r8.println(l13);
        r9 = System.out;
        l14 = _2_7;
        r9.println(l14);
        r10 = System.out;
        l15 = _3_0;
        r10.println(l15);
    }

    public static void func1(int  i0)
    {

        byte b1;
        int i2, i3;
        long l4, l5, l6, l7, l8, l9, l10, l11, l12, l13, l14, l15, l16, l17, l18, l19;
        l4 = _2_0;
        l5 = l4 + 1L;
        _2_0 = l5;

        if (i0 == 0)
        {
            b1 = (byte) (byte) 0;
            l8 = _2_2;
            l9 = l8 + 1L;
            _2_2 = l9;
            i0 = b1;
        }
        else
        {
            l6 = _2_1;
            l7 = l6 + 1L;
            _2_1 = l7;
        }

        l10 = _2_3;
        l11 = l10 + 1L;
        _2_3 = l11;

        while (true)
        {
            i2 = i0 % 4;
            l12 = _2_4;
            l13 = l12 + 1L;
            _2_4 = l13;

            if (i2 == 0)
            {
                l14 = _2_5;
                l15 = l14 + 1L;
                _2_5 = l15;
                l18 = _2_7;
                l19 = l18 + 1L;
                _2_7 = l19;
                return;
            }

            i3 = i0 / 4;
            i0 = i3;
            l16 = _2_6;
            l17 = l16 + 1L;
            _2_6 = l17;
        }
    }

    public Test1()
    {

        long l0, l1;
        l0 = _3_0;
        l1 = l0 + 1L;
        _3_0 = l1;
    }
}
