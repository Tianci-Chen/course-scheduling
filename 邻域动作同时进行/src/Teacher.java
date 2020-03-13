import java.util.ArrayList;

public class Teacher {
    String id;
    ArrayList<Curriculum> cur = new ArrayList<>();
    ArrayList<Arrange> preserved = new ArrayList<>();
    Integer[] t_table = new Integer[30];                   //教师时间表
    int penalty;                  //单个教师的惩罚值
    Integer[] delta=new Integer[10];    //教师惩罚值改变量
    Integer[] del=new Integer[10];
    int curIndex;

    public Teacher(String s1) {
        this.id = s1;
    }

    public void add_Preserved(String s2) {
        String[] s = s2.split("&");
        for (String str : s) {
            Arrange arrange = new Arrange(str);
            preserved.add(arrange);
        }
    }

    public void addCurriculum(Curriculum curriculum) {
        cur.add(curriculum);
    }

    public int teacher_Conflicts() {
        int conflicts = 0;
        for (int i = 0; i < cur.size(); i++) {
            for (int j = i + 1; j < cur.size(); j++) {
                conflicts += cur.get(i).curriculum_Conflicts(cur.get(j));
            }
        }
        return conflicts;
    }

    public int teacher_Conflicts_Preserved() {
        int conflicts = 0;
        conflicts += teacher_Conflicts();
        for (int i = 0; i < preserved.size(); i++) {
            for (int j = 0; j < cur.size(); j++) {
                conflicts += cur.get(j).arr_cur_Conflicts(preserved.get(i), cur.get(j));
            }
        }
        return conflicts;
    }

    public void init_Time_Table(){
        for (int i = 0; i < 30; i++) {
            t_table[i] = 0;
        }
        for (int i = 0; i < cur.size(); i++) {
            cur.get(i).time_Table(t_table);
        }
        for (int i = 0; i < preserved.size(); i++) {
            preserved.get(i).make_Time_Table(t_table);
        }
    }

    public void print_Table() {                           //打印时间表
        for (int i = 0; i < 30; i++) {
            if (i % 5 == 0) {
                System.out.println();
            }
            System.out.print(Integer.toBinaryString(t_table[i]) + " ");
        }
    }

    public int teacher_Continuous_Penalty() {       //教师连续上课惩罚值
        init_Time_Table();
        penalty=calcPenalty();
        return penalty;
    }
    public int calcPenalty(){
        int pen=0;
        for (int j = 0; j < 6; j++) {
            int t1 = t_table[5 * j] & t_table[5 * j + 1];
            int t2 = t_table[5 * j + 2] & t_table[5 * j + 3];
            int n = Integer.bitCount(t1);
            int m = Integer.bitCount(t2);
            int p = Integer.bitCount(t1 & t2);
            pen += n + m + 2 * p;
        }
        return pen;
    }
}