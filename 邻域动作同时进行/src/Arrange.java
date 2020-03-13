public class Arrange {
    int week;
    int day;
    int hour;

    Arrange() {
        week = 0;
        day = 0;
        hour = 0;
    }

    public void copy(Arrange arrange) {
        week=arrange.week;
        day = arrange.day;
        hour = arrange.hour;
    }

    Arrange(String s) {
        String[] string = s.split("_");
        week = Integer.parseInt(string[1], 2);
        day = Integer.parseInt(string[2], 2);
        hour = Integer.parseInt(string[3], 2);
    }

    public String toString() {
        return Integer.toBinaryString(week) + "_" + Integer.toBinaryString(day) + "_" + Integer.toBinaryString(hour);
    }

    public int position_Of_Day() {
        double x = Math.log(day) / Math.log(2);
        int m = 6 - (int) x;
        return m;
    }

    public int position_Of_Hour() {
        double x = Math.log(hour) / Math.log(2);
        int m = 4 - (int) x;
        return m;
    }

    public int last_One_Of_Week() {
        Integer[] weeks = {0b10000000000000000000, 0b01000000000000000000, 0b00100000000000000000, 0b00010000000000000000, 0b00001000000000000000, 0b00000100000000000000,
                0b00000010000000000000, 0b00000001000000000000, 0b00000000100000000000, 0b00000000010000000000, 0b00000000001000000000, 0b00000000000100000000, 0b00000000000010000000,
                0b00000000000001000000, 0b00000000000000100000, 0b00000000000000010000, 0b00000000000000001000, 0b00000000000000000100, 0b00000000000000000010, 0b00000000000000000001};
        int i=19;
        for (; i >= 0; i--) {
            if ((week & weeks[i]) != 0){
                break;
            }
        }
        return i;
    }

    public void make_Time_Table(Integer[] t_table) {
        Integer[] hours1 = {0b10000, 0b01000, 0b00100, 0b00010, 0b00001};
        Integer[] days1 = {0b1000000, 0b0100000, 0b0010000, 0b0001000, 0b0000100, 0b0000010};
        if (Integer.bitCount(day) == 1) {
            int j = position_Of_Day();
            if (Integer.bitCount(hour) == 1) {
                int k = position_Of_Hour();
                t_table[5 * j + k] |= week;
            } else {
                for (int k = 0; k < hours1.length; k++) {
                    if ((hour & hours1[k]) != 0) {
                        t_table[5 * j + k] |= week;
                    }
                }
            }
        } else {
            for (int j = 0; j < days1.length; j++) {
                if ((day & days1[j]) != 0) {
                    if (Integer.bitCount(hour) == 1) {
                        int k = position_Of_Hour();
                        t_table[5 * j + k] |= week;
                    } else {
                        for (int k = 0; k < hours1.length; k++) {
                            if ((hour & hours1[k]) != 0) {
                                t_table[5 * j + k] |= week;
                            }
                        }
                    }
                }
            }
        }
    }

    int calcConflict(Arrange aa) {
        int conflictWeek = Integer.bitCount(week & aa.week);
        int conflictDay = Integer.bitCount(day & aa.day);
        int conflictHour = Integer.bitCount(hour & aa.hour);
        if (conflictWeek * conflictDay * conflictHour == 0) {
            return 0;
        } else
            return conflictWeek * conflictDay * conflictHour;       //返回  conflictWeek中1的个数 * conflictDay中1的个数 * conflictHour中1的个数
    }
}