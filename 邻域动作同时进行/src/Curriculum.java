import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Curriculum {
    String task_no;                           //任务号
    Teacher teacher;             //授课教师
    ArrayList<Group> groups = new ArrayList<>();               //上课班级
    String cur_name;            //课程名称
    double cur_credit;              //学分
    int cur_times;              //课时
    int p_num;                  //人数
    ArrayList<Arrange> arranges = new ArrayList<>();   //安排
    Arrange temp_arrange1 = new Arrange();
    Arrange temp_arrange2 = new Arrange();                    //存储改变的安排
    Arrange copy_arrange1 = new Arrange();
    Arrange copy_arrange2 = new Arrange();                 //存储原始的安排
    ArrayList<Integer> temp_week = new ArrayList<>();
    ArrayList<Integer> copy_week = new ArrayList<>();
    Integer[][] taboo = new Integer[20][20];                 //被禁忌的安排
    Integer[][] tabooWeek = new Integer[20][20];
    int delta;                                     //惩罚变化值
    int best_count;
    int sign = 0;                     //改变节次或周次的标志
    ArrayList<ArrayList<Integer>> alter_week = new ArrayList<>();     //备选周次
    Random random = new Random();


    Curriculum() {
    }

    Integer[] hours1 = {0b10000, 0b01000, 0b00100, 0b00010};
    Integer[] hours2 = {0b10000, 0b01000};
    Integer[] hours3 = {0b00001};
    Integer[] days1 = {0b1000000, 0b0100000, 0b0010000, 0b0001000, 0b0000100};
    Integer[] weeks = {
            0b10000000000000000000,
            0b01000000000000000000,
            0b00100000000000000000,
            0b00010000000000000000,
            0b00001000000000000000,
            0b00000100000000000000,
            0b00000010000000000000,
            0b00000001000000000000,
            0b00000000100000000000,
            0b00000000010000000000,
            0b00000000001000000000,
            0b00000000000100000000,
            0b00000000000010000000,
            0b00000000000001000000,
            0b00000000000000100000,
            0b00000000000000010000,
            0b00000000000000001000,
            0b00000000000000000100,
            0b00000000000000000010,
            0b00000000000000000001};

    void curriculum_Arrangement() {
        int q = 16;
        if (cur_times >= 64)
            q = 18;
        if (cur_times <= 24) {
            Arrange arrange = new Arrange();
            int w = cur_times / 2;
            curriculum_Arrangement(arrange, w, days1, q, false);
            if (arrange.week == 0) {
                curriculumArrangeEve(arrange, q);
            }
            arranges.add(arrange);
        }
        if (cur_times > 24) {
            if (cur_times % 4 == 0) {
                int w = cur_times / 4;
                two_Arrangements(w, w, q);
            } else {
                int w1 = (cur_times / 2 + 1) / 2, w2 = cur_times / 2 - w1;
                two_Arrangements(w1, w2, q);
            }
        }
    }

    public void curriculumArrangeEve(Arrange arrange, int q) {
        int w = 0;
        if (cur_times % 3 == 0) {
            w = cur_times / 3;
        } else {
            if ((cur_times + 1) % 3 == 0) {
                w = (cur_times + 1) / 3;
            }
            if ((cur_times - 1) % 3 == 0) {
                w = (cur_times - 1) / 3;
            }
        }
        curriculum_Arrangement(arrange, w, days1, q, true);
    }

    public void two_Arrangements(Integer w1, Integer w2, int q) {
        Arrange arrange1 = new Arrange();
        Arrange arrange2 = new Arrange();
        int n = curriculum_Arrangement(arrange1, w1, days1, q, false);
        if (n != 0) {
            int m = arrange1.position_Of_Day();
            if (m <= days1.length - 3) {
                curriculum_Arrangement(arrange2, w2, init_Day(m, days1), q, false);
            }
        }
        if (n == 0 || arrange2.week == 0) {
            w2 = cur_times / 5;
            if (w2 % 2 != 0) {
                w2 -= 1;
            }
            w1 = (cur_times - 3 * w2) / 2;
            arrange1 = new Arrange();
            curriculum_Arrangement(arrange1, w1, days1, q, false);
            if (arrange1.week != 0) {
                int m = arrange1.position_Of_Day();
                curriculum_Arrangement(arrange2, w2, initDayEve(m, days1), q, true);
            }
            if (arrange2.week == 0) {
                arrange1 = new Arrange();
                curriculum_Arrangement(arrange1, w2, days1, q, true);
                int m = arrange1.position_Of_Day();
                curriculum_Arrangement(arrange2, w1, initDayEve(m, days1), q, false);
            }
        }
        if (arrange2.week == 0) {
            arrange1 = new Arrange();
            double w = cur_times / 6.0;
            int ww = cur_times / 6;
            if (w > ww) {
                w1 = ww;
                w2 = w1 + 1;
            } else {
                w1 = ww;
                w2 = ww;
            }
            curriculum_Arrangement(arrange1, w1, days1, q, true);
            int m = arrange1.position_Of_Day();
            curriculum_Arrangement(arrange2, w2, init_Day(m, days1), q, true);
        }
        arranges.add(arrange1);
        arranges.add(arrange2);
    }

    public Integer[] init_Day(int m, Integer[] p) {
        int j = 0;
        Integer[] day = new Integer[p.length - 2 - m];
        for (int i = m + 2; i < p.length; i++) {
            day[j++] = p[i];
        }
        return day;
    }

    public Integer[] initDayEve(int m, Integer[] p) {
        Integer[] day;
        if (m == 0 || m == 4) {
            day = new Integer[p.length - 2];
        } else {
            day = new Integer[p.length - 3];
        }
        int j = 0;
        for (int i = m - 2; i > 0; i--) {
            day[j++] = p[i];
        }
        for (int i = m + 2; i < p.length; i++) {
            day[j++] = p[i];
        }
        return day;
    }

    public int curriculum_Arrangement(Arrange arrange, int w, Integer[] day, int n, boolean eve) {
        int ws = 0;
        ArrayList<Integer> d = new ArrayList<>(Arrays.asList(day));
        //   Collections.shuffle(d, random);
        for (int i = 0; i < d.size(); i++) {
            arrange.day = d.get(i);
            ArrayList<Integer> h = new ArrayList<Integer>(is_Thursday(arrange.day, eve));
            for (int j = 0; j < h.size(); j++) {
                arrange.hour = h.get(j);
                for (int m = 0; m < n; m++) {
                    arrange.week += weeks[m];
                    if (curriculum_Conflicts(arrange) != 0) {
                        arrange.week -= weeks[m];
                    } else {
                        if ((ws = Integer.bitCount(arrange.week)) == w) {
                            break;
                        }
                    }
                }
                if (ws == w) {
                    break;
                } else {
                    arrange.week = 0;
                    ws = 0;
                }
            }
            if (ws == w) {
                break;
            } else {
                arrange.week = 0;
                ws = 0;
            }
        }
        return arrange.week;
    }

    public ArrayList is_Thursday(int n, boolean eve) {
        ArrayList<Integer> h;
        if (eve) {
            h = new ArrayList<>(Arrays.asList(hours3));
        } else {
            if ((n & 0b1000) != 0 || (n & 0b10) != 0) {
                h = new ArrayList<>(Arrays.asList(hours2));
            } else {
                h = new ArrayList<>(Arrays.asList(hours1));
            }
        }
        //    Collections.shuffle(h, random);
        return h;
    }

    public int curriculum_Conflicts(Curriculum curriculum) {                          //课程与课程之间的冲突，计算冲突时所用
        int s = 0, m;
        for (int k = 0; k < this.arranges.size(); k++) {
            Arrange arr_i = this.arranges.get(k);
            for (int p = 0; p < curriculum.arranges.size(); p++) {
                Arrange arr_j = curriculum.arranges.get(p);
                m = arr_i.calcConflict(arr_j);
             /*   if (m != 0) {
                    System.out.println(this.task_no + ": " + arr_i + " & " + curriculum.task_no + ": " + arr_j + " : " + m + "conflicts");
                } */
                s += m;
            }
        }
        return s;
    }

    public int arr_cur_Conflicts(Arrange arrange, Curriculum curriculum) {               //安排与课程之间的冲突
        int s = 0;
        for (int p = 0; p < curriculum.arranges.size(); p++) {
            Arrange arr_j = curriculum.arranges.get(p);
            s += arrange.calcConflict(arr_j);
        }
        return s;
    }

    public int curriculum_Conflicts(Arrange arrange) {                            //课程与安排之间的冲突,用于生成初始解时判断冲突所用
        int conflicts = 0;
        for (int i = 0; i < teacher.preserved.size(); i++) {
            conflicts += arrange.calcConflict(teacher.preserved.get(i));
        }
        for (int i = teacher.cur.size() - 2; i >= 0; i--) {
            conflicts += arr_cur_Conflicts(arrange, teacher.cur.get(i));
        }
        for (int i = 0; i < groups.size(); i++) {
            for (int j = 0; j < groups.get(i).preserved.size(); j++) {
                conflicts += arrange.calcConflict(groups.get(i).preserved.get(j));
            }
            for (int j = groups.get(i).cur.size() - 2; j >= 0; j--) {
                conflicts += arr_cur_Conflicts(arrange, groups.get(i).cur.get(j));
            }
        }
        return conflicts;
    }

    public int weekend_Penalty_Value() {                                     //计算学院所安排课程的周末惩罚值
        int a = 0;
        for (int i = 0; i < arranges.size(); i++) {
            if ((arranges.get(i).day & 0b10) != 0) {
                a += Integer.bitCount(arranges.get(i).week);
            }
        }
        return a;
    }

    public int evening_Penalty_Value() {
        int d = 0;
        for (int i = 0; i < arranges.size(); i++) {
            if ((arranges.get(i).hour & 0b1) != 0) {
                d += Integer.bitCount(arranges.get(i).week);
            }
        }
        return d;
    }

    public void time_Table(Integer[] t_table) {
        for (int i = 0; i < arranges.size(); i++) {
            Arrange arr_i = arranges.get(i);
            int j = arr_i.position_Of_Day();
            int k = arr_i.position_Of_Hour();
            t_table[5 * j + k] |= arr_i.week;
        }
    }

    public void init_arrange() {
        temp_arrange1 = new Arrange();
        temp_arrange2 = new Arrange();
        copy_arrange1 = new Arrange();
        copy_arrange2 = new Arrange();
        temp_week = new ArrayList<>();
        copy_week = new ArrayList<>();
    }

    public void updateCurIndex() {
        teacher.curIndex = teacher.cur.indexOf(this);
        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);
            group.curIndex = group.cur.indexOf(this);
        }
    }

    public void neighborhoodAction(Solver solver) {
        delta = 5000;
        init_arrange();
        temp_week.add(0);
        updateCurIndex();
        if (cur_times <= 24) {
            if (arranges.get(0).hour != 1) {
                change_Day_Hour_One(solver);
            }
            change_Week_One(solver);
        } else {
            if (arranges.get(0).hour != 1 && arranges.get(1).hour != 1) {
                change_Day_Hour_Two(solver);
            }
            temp_week.add(0);
            change_Week_Two(solver);
        }
    }

    public void change_Day_Hour_One(Solver solver) {
        copy_arrange1.copy(arranges.get(0));
        for (int i = 0; i < days1.length; i++) {
            arranges.get(0).day = days1[i];
            ArrayList<Integer> h = new ArrayList<Integer>(is_Thursday(arranges.get(0).day, false));
            for (int j = 0; j < h.size(); j++) {
                arranges.get(0).hour = h.get(j);
                if (is_Original()) {
                    continue;
                }
                if (is_Taboo(solver)) {
                    continue;
                }
                ArrayList<Integer> array = new ArrayList<>();
                array.add(i);
                array.add(j);
                make_Delta(array);
            }
        }
        arranges.set(0, copy_arrange1);
    }

    public void change_Day_Hour_Two(Solver solver) {
        copy_arrange1.copy(arranges.get(0));
        copy_arrange2.copy(arranges.get(1));
        for (int i = 0; i < days1.length - 2; i++) {
            arranges.get(0).day = days1[i];
            ArrayList<Integer> h1 = new ArrayList<Integer>(is_Thursday(arranges.get(0).day, false));
            for (int j = 0; j < h1.size(); j++) {
                arranges.get(0).hour = h1.get(j);
                Integer[] day = init_Day(i, days1);
                for (int m = 0; m < day.length; m++) {
                    arranges.get(1).day = day[m];
                    ArrayList<Integer> h2 = new ArrayList<>(is_Thursday(arranges.get(1).day, false));
                    for (int n = 0; n < h2.size(); n++) {
                        arranges.get(1).hour = h2.get(n);
                        if (is_Original()) {
                            continue;
                        }
                        if (is_Taboo(solver)) {
                            continue;
                        }
                        ArrayList<Integer> array = new ArrayList<>();
                        array.add(i);
                        array.add(j);
                        array.add(m + i + 2);
                        array.add(n);
                        make_Delta(array);
                    }
                }
            }
        }
        arranges.set(0, copy_arrange1);
        arranges.set(1, copy_arrange2);
    }

    public boolean is_Taboo(Solver solver) {
        Arrange arrange1 = arranges.get(0);
        int i1 = arrange1.position_Of_Day();
        int j1 = arrange1.position_Of_Hour();
        if (cur_times <= 24) {
            if (taboo[4 * i1 + j1][0] > solver.iterator) {
                return true;
            } else {
                return false;
            }
        } else {
            Arrange arrange2 = arranges.get(1);
            int i2 = arrange2.position_Of_Day();
            int j2 = arrange2.position_Of_Hour();
            if (taboo[4 * i1 + j1][4 * i2 + j2] > solver.iterator) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void make_Delta(ArrayList<Integer> array) {
        int d = penaltyChangeOne(array);
        if (d < delta) {
            best_count = 1;
            delta = d;
            temp_arrange1.copy(arranges.get(0));
            if (cur_times > 24) {
                temp_arrange2.copy(arranges.get(1));
            }
            updateTeacherGroupDelta();
            sign = 0;
        }
        if (d == delta) {
            best_count++;
            if ((int) (Math.random() * best_count) == 0) {
                temp_arrange1.copy(arranges.get(0));
                if (cur_times > 24) {
                    temp_arrange2.copy(arranges.get(1));
                }
                updateTeacherGroupDelta();
            }
        }
    }

    public void updateTeacherGroupDelta() {
        teacher.delta[teacher.curIndex] = teacher.del[teacher.curIndex];
        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);
            group.delta[group.curIndex] = group.del[group.curIndex];
        }
    }

    public int penaltyChangeOne(ArrayList<Integer> array) {
        int p = 0;
        if (conflicts_After_Change() != 0) {
            return 10000000;
        } else {
            if ((copy_arrange1.day & 0b10) != 0) {
                p = -Integer.bitCount(copy_arrange1.week);
            }
            if (cur_times > 24) {
                if ((copy_arrange2.day & 0b10) != 0) {
                    p = -Integer.bitCount(copy_arrange2.week);
                }
            }
            p += groupPenaltyDelta(array);
            p += teacherPenaltyDelta(array);
            return p;
        }
    }

    public int teacherPenaltyDelta(ArrayList<Integer> array) {                    //课程安排改变后教师的软约束惩罚值改变量
        int i1 = copy_arrange1.position_Of_Day();
        int j1 = copy_arrange1.position_Of_Hour();
        int i2 = 0;
        int j2 = 0;
        teacher.t_table[5 * i1 + j1] -= arranges.get(0).week;
        if (cur_times > 24) {
            i2 = copy_arrange2.position_Of_Day();
            j2 = copy_arrange2.position_Of_Hour();
            teacher.t_table[5 * i2 + j2] -= arranges.get(1).week;
        }
        for (int i = 0; i < array.size(); i++, i++) {
            teacher.t_table[5 * array.get(i) + array.get(i + 1)] |= arranges.get(i / 2).week;
        }
        int d = teacher.calcPenalty() - teacher.penalty;
        teacher.del[teacher.curIndex] = d;
        for (int i = 0; i < array.size(); i++, i++) {
            teacher.t_table[5 * array.get(i) + array.get(i + 1)] -= arranges.get(i / 2).week;
        }
        teacher.t_table[5 * i1 + j1] += arranges.get(0).week;
        if (cur_times > 24) {
            teacher.t_table[5 * i2 + j2] += arranges.get(1).week;
        }
        return d;
    }

    public int groupPenaltyDelta(ArrayList<Integer> array) {
        int gDelta = 0;
        for (int p = 0; p < groups.size(); p++) {
            Group group = groups.get(p);
            int i1 = copy_arrange1.position_Of_Day();
            int j1 = copy_arrange1.position_Of_Hour();
            int i2 = 0;
            int j2 = 0;
            group.t_table[5 * i1 + j1] -= arranges.get(0).week;
            if (cur_times > 24) {
                i2 = copy_arrange2.position_Of_Day();
                j2 = copy_arrange2.position_Of_Hour();
                group.t_table[5 * i2 + j2] -= arranges.get(1).week;
            }
            for (int i = 0; i < array.size(); i++, i++) {
                group.t_table[5 * array.get(i) + array.get(i + 1)] |= arranges.get(i / 2).week;
            }
            int d = group.calcPenalty() - group.penalty;
            group.del[group.curIndex] = d;
            gDelta += d;
            for (int i = 0; i < array.size(); i++, i++) {
                group.t_table[5 * array.get(i) + array.get(i + 1)] -= arranges.get(i / 2).week;
            }
            group.t_table[5 * i1 + j1] += arranges.get(0).week;
            if (cur_times > 24) {
                group.t_table[5 * i2 + j2] += arranges.get(1).week;
            }
        }
        return gDelta;
    }


    public boolean is_Original() {
        boolean bool = false;
        if (arranges.get(0).day == copy_arrange1.day && arranges.get(0).hour == copy_arrange1.hour) {
            bool = true;
        }
        if (cur_times > 24) {
            if (arranges.get(1).day != copy_arrange2.day || arranges.get(1).hour != copy_arrange2.hour) {
                bool = false;
            }
        }
        return bool;
    }

    public void makeAlternativeWeek() {
        for (int i = 0; i < arranges.size(); i++) {
            ArrayList<Integer> w1 = new ArrayList<>();
            int week1 = arranges.get(i).week;
            w1.add(week1);
            int m = arranges.get(i).last_One_Of_Week();
            for (int j = 0; m < 15 && j < 20; j++) {
                if ((week1 & weeks[j]) != 0) {
                    week1 -= weeks[j];
                    week1 += weeks[++m];
                    w1.add(week1);
                }
            }
            alter_week.add(w1);
        }
    }

    public void change_Week_One(Solver solver) {
        copy_week.add(arranges.get(0).week);
        for (int i = 0; i < alter_week.get(0).size(); i++) {
            if (alter_week.get(0).get(i) == copy_week.get(0)) {
                continue;
            }
            if (tabooWeek[i][0] > solver.iterator) {
                continue;
            }
            arranges.get(0).week = alter_week.get(0).get(i);
            make_Delta_Week(solver);
        }
        arranges.get(0).week = copy_week.get(0);
    }

    public void change_Week_Two(Solver solver) {
        copy_week.add(arranges.get(0).week);
        copy_week.add(arranges.get(1).week);
        for (int i = 0; i < alter_week.get(0).size(); i++) {
            arranges.get(0).week = alter_week.get(0).get(i);
            for (int j = 0; j < alter_week.get(1).size(); j++) {
                if (arranges.get(0).week == copy_week.get(0) && alter_week.get(1).get(j) == copy_week.get(1)) {
                    continue;
                }
                if (tabooWeek[i][j] > solver.iterator) {
                    continue;
                }
                arranges.get(1).week = alter_week.get(1).get(j);
                make_Delta_Week(solver);
            }
        }
        arranges.get(0).week = copy_week.get(0);
        arranges.get(1).week = copy_week.get(1);
    }

    public void make_Delta_Week(Solver solver) {
        int d = penaltyChangeTwo();
        if (d < delta) {
            best_count = 1;
            delta = d;
            temp_week.set(0, arranges.get(0).week);
            if (cur_times > 24) {
                temp_week.set(1, arranges.get(1).week);
            }
            updateTeacherGroupDelta();
            sign = 1;
        }
        if (d == delta) {
            best_count++;
            if ((int) (Math.random() * best_count) == 0) {
                temp_week.set(0, arranges.get(0).week);
                if (cur_times > 24) {
                    temp_week.set(1, arranges.get(1).week);
                }
                updateTeacherGroupDelta();
                sign = 1;
            }
        }
    }

    public int penaltyChangeTwo() {
        int p = 0;
        if (conflicts_After_Change() != 0) {
            return 10000000;
        } else {
            p += groupPenaltyDeltaWeek();
            p += teacherPenaltyDeltaWeek();
            return p;
        }
    }

    public int groupPenaltyDeltaWeek() {
        Integer[] i1 = new Integer[2];
        Integer[] j1 = new Integer[2];
        Integer[] index = new Integer[2];
        int gDelta = 0;
        for (int g = 0; g < groups.size(); g++) {
            Group group = groups.get(g);
            for (int i = 0; i < arranges.size(); i++) {
                i1[i] = arranges.get(i).position_Of_Day();
                j1[i] = arranges.get(i).position_Of_Hour();
                index[i] = 5 * i1[i] + j1[i];
                group.t_table[index[i]] -= copy_week.get(i);
                group.t_table[index[i]] += arranges.get(i).week;
            }
            int d = group.calcPenalty() - group.penalty;
            group.del[group.curIndex] = d;
            gDelta += d;
            for (int i = 0; i < arranges.size(); i++) {
                group.t_table[index[i]] -= arranges.get(i).week;
                group.t_table[index[i]] += copy_week.get(i);
            }
        }
        return gDelta;
    }

    public int teacherPenaltyDeltaWeek() {
        Integer[] i1 = new Integer[2];
        Integer[] j1 = new Integer[2];
        Integer[] index = new Integer[2];
        for (int i = 0; i < arranges.size(); i++) {
            i1[i] = arranges.get(i).position_Of_Day();
            j1[i] = arranges.get(i).position_Of_Hour();
            index[i] = 5 * i1[i] + j1[i];
            teacher.t_table[index[i]] -= copy_week.get(i);
            teacher.t_table[index[i]] += arranges.get(i).week;
        }
        int d = teacher.calcPenalty() - teacher.penalty;
        teacher.del[teacher.curIndex] = d;
        for (int i = 0; i < arranges.size(); i++) {
            teacher.t_table[index[i]] -= arranges.get(i).week;
            teacher.t_table[index[i]] += copy_week.get(i);
        }
        return d;
    }

    public int conflicts_After_Change() {
        int n = 0;
        n += teacher.teacher_Conflicts_Preserved();
        for (int i = 0; i < groups.size(); i++) {
            n += groups.get(i).student_Conflicts_Preserved();
        }
        return n;
    }

    public void make_Taboo(Solver solver) {
        Arrange arrange1 = arranges.get(0);
        int i1 = arrange1.position_Of_Day();
        int j1 = arrange1.position_Of_Hour();
        if (cur_times <= 24) {
            taboo[4 * i1 + j1][0] = solver.iterator + (int)(Math.random()*21+10);
        } else {                                     //(int)(Math.random()*10+1);
            Arrange arrange2 = arranges.get(1);        //(int)(Math.random()*21+10);
            int i2 = arrange2.position_Of_Day();       //(int)(Math.random()*41+30);
            int j2 = arrange2.position_Of_Hour();
            taboo[4 * i1 + j1][4 * i2 + j2] = solver.iterator + (int)(Math.random()*21+10);
        }
    }

    public void make_Taboo_Week(Solver solver) {
        int i = alter_week.get(0).indexOf(arranges.get(0).week);
        if (cur_times <= 24) {
            tabooWeek[i][0] = solver.iterator + (int)(Math.random()*21+10);
        } else {
            int j = alter_week.get(1).indexOf(arranges.get(1).week);
            tabooWeek[i][j] = solver.iterator + (int)(Math.random()*21+10);
        }
    }

    public String toString() {
        String s = "", ss = "";
        for (int i = 0; i < groups.size(); i++) {
            s += groups.get(i).id + ",";
        }
        for (int j = 0; j < arranges.size(); j++) {
            ss += "," + arranges.get(j);
        }
        return task_no + "," + cur_name + "," + cur_credit + "," + s + p_num + "," + teacher.id + "-主讲," + cur_times + ss;
    }
}