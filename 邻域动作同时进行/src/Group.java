import java.util.ArrayList;

public class Group {
    String id;
    ArrayList<Arrange> preserved = new ArrayList<>();
    ArrayList<Curriculum> cur = new ArrayList<>();
    Integer[] t_table = new Integer[30];                               //时间表
    int penalty;           //单个班级惩罚值
    Integer[] delta=new Integer[20];    //班级惩罚值改变量
    Integer[] del=new Integer[20];
    int curIndex;

    public Group(String s1) {                                   //构造函数
        this.id = s1;
    }

    public void add_Preserved(String s2) {
        String[] s = s2.split("&");
        for (String str : s) {
            Arrange arrange = new Arrange(str);
            preserved.add(arrange);
        }
    }

    public void addCurriculum(Curriculum curriculum) {                                 //添加班级所上课程
        cur.add(curriculum);
    }

    public void init_Time_Table() {                       //基础课程的安排可能在周末，故t_table的长度暂定为24
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

    public int student_Conflicts() {
        int conflicts = 0;
        for (int i = 0; i < cur.size(); i++) {
            for (int j = i + 1; j < cur.size(); j++) {
                conflicts += cur.get(i).curriculum_Conflicts(cur.get(j));
            }
        }
        return conflicts;
    }

    public int student_Conflicts_Preserved() {
        int conflicts = 0;
        conflicts += student_Conflicts();
        for (int i = 0; i < preserved.size(); i++) {
            for (int j = 0; j < cur.size(); j++) {
                conflicts += cur.get(j).arr_cur_Conflicts(preserved.get(i), cur.get(j));
            }
        }
        return conflicts;
    }

    public int student_Average_Penalty() {
        init_Time_Table();
        penalty=calcPenalty();
        return penalty;
    }

    public int calcPenalty(){
        int pen=0;
        for (int j = 0; j < 5; j++) {
            int t1 = t_table[5 * j] | t_table[5 * j + 1] | t_table[5 * j + 2] | t_table[5 * j + 3] | t_table[5 * j + 4];
            int n = t1 & 0b11111111111111110000;
            int m = 16 - Integer.bitCount(n);
            pen += m;
        }
        return pen;
    }
}