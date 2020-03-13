import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

public class Solver {
    ArrayList<Curriculum> c = new ArrayList<Curriculum>();    //课程
    ArrayList<Teacher> t = new ArrayList<Teacher>();          //教师
    ArrayList<Group> g = new ArrayList<Group>();              //班级
    TreeMap<String, Group> gMap = new TreeMap<>();
    TreeMap<String, Teacher> tMap = new TreeMap<>();
    int penalty_value;           //软约束惩罚值
    int conflicts;               //计算冲突值
    int iterator;                 //迭代次数
    int v1, v2, v3, v4;                 //软约束惩罚函数系数
    long startTime;
    long bestTime;
    int bestIter;


    public void readFile() {
        try {
            FileReader f = new FileReader("计算机学院需排课程算法.txt");
            BufferedReader b = new BufferedReader(f);
            String str = " ";
            while ((str = b.readLine()) != null) {
                String[] s = str.split(",");
                Curriculum curriculum = new Curriculum();
                curriculum.task_no = new String(s[0]);
                curriculum.cur_name = new String(s[1]);
                curriculum.cur_credit = Double.parseDouble(s[2]);
                String[] ss = s[3].split("&");
                for (String gName : ss) {
                    Group group = gMap.get(gName);
                    if (group == null) {
                        group = new Group(gName);
                        gMap.put(gName, group);
                        g.add(group);
                    }
                    group.addCurriculum(curriculum);
                    curriculum.groups.add(group);
                }
                curriculum.p_num = Integer.parseInt(s[4]);
                ss = s[5].split("-");

                Teacher teacher = tMap.get(ss[0]);
                if (teacher == null) {
                    teacher = new Teacher(ss[0]);
                    tMap.put(ss[0], teacher);
                    t.add(teacher);
                }
                teacher.addCurriculum(curriculum);
                curriculum.teacher = teacher;
                curriculum.cur_times = Integer.parseInt(s[6]);
                c.add(curriculum);
            }
            f.close();
            b.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void teacher_Preserved() {
        try {
            FileReader f = new FileReader("计算机学院教师preserved.txt");
            BufferedReader b = new BufferedReader(f);
            String str = " ";
            while ((str = b.readLine()) != null) {
                String[] s = str.split(",");
                String[] ss = s[5].split("-");
                Teacher teacher = tMap.get(ss[0]);
                if (teacher != null) {
                    teacher.add_Preserved(s[7]);
                }
            }
            f.close();
            b.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void group_Preserved() {
        try {
            FileReader f = new FileReader("计算机学院学生preserved.txt");
            BufferedReader b = new BufferedReader(f);
            String str = " ";
            while ((str = b.readLine()) != null) {
                String[] s = str.split(",");
                String[] ss = s[3].split("&");
                for (String gName : ss) {
                    Group group = gMap.get(gName);
                    if (group != null) {
                        group.add_Preserved(s[7]);
                    }
                }
            }
            f.close();
            b.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int count_Conflicts() {
        conflicts = 0;
        for (int i = 0; i < this.t.size(); i++) {
            conflicts += this.t.get(i).teacher_Conflicts();
        }
        for (int i = 0; i < this.g.size(); i++) {
            conflicts += this.g.get(i).student_Conflicts();
        }
        return conflicts;
    }

    public void arrangement() {                                               //先排序后安排
        Collections.sort(c, new Comparator<Curriculum>() {
            public int compare(Curriculum o1, Curriculum o2) {
                if (o1.cur_times < o2.cur_times) {
                    return 1;
                }
                if (o1.cur_times > o2.cur_times) {
                    return -1;
                }
                return 0;
            }
        });
        for (int i = 0; i < c.size(); i++) {
            c.get(i).curriculum_Arrangement();
            System.out.println(c.get(i));
        }
    }

    public int count_Soft_Constraints() {                             //后期更改可引入penalty_value
        int A = 0;
        int B = 0;
        int C = 0;
        int D = 0;
        for (int i = 0; i < c.size(); i++) {
            A += c.get(i).weekend_Penalty_Value();
            D += c.get(i).evening_Penalty_Value();
        }
        for (int i = 0; i < t.size(); i++) {
            B += t.get(i).teacher_Continuous_Penalty();
        }
        for (int i = 0; i < g.size(); i++) {
            C += g.get(i).student_Average_Penalty();
        }
        return v1 * A + v2 * B + v3 * C + v4 * D;
    }

    public void updateCurriculumDelta(Solver solver, Curriculum curriculum) {
        TreeMap<String, Curriculum> cMap = new TreeMap<>();         //记录已安排的课程
        Teacher teacher = curriculum.teacher;
        for (int i = 0; i < teacher.cur.size(); i++) {
            Curriculum curriculum1 = teacher.cur.get(i);
            curriculum1.neighborhoodAction(solver);
            cMap.put(curriculum1.task_no, curriculum1);
        }
        for (int i = 0; i < curriculum.groups.size(); i++) {
            Group group = curriculum.groups.get(i);
            for (int j = 0; j < group.cur.size(); j++) {
                Curriculum curriculum1 = group.cur.get(j);
                if (cMap.get(curriculum1.task_no) == null) {
                    curriculum1.neighborhoodAction(solver);
                    cMap.put(curriculum1.task_no, curriculum1);
                }
            }
        }
    }

    public void updateTeacherPenalty(Curriculum curriculum) {
        Teacher teacher = curriculum.teacher;
        teacher.curIndex = teacher.cur.indexOf(curriculum);
        for (int i = 0; i < curriculum.arranges.size(); i++) {
            Arrange arrange1 = curriculum.arranges.get(i);
            int i1 = arrange1.position_Of_Day();
            int j1 = arrange1.position_Of_Hour();
            teacher.t_table[5 * i1 + j1] -= arrange1.week;
        }
        int i1 = curriculum.temp_arrange1.position_Of_Day();
        int j1 = curriculum.temp_arrange1.position_Of_Hour();
        teacher.t_table[5 * i1 + j1] += curriculum.temp_arrange1.week;
        if (curriculum.cur_times > 24) {
            i1 = curriculum.temp_arrange2.position_Of_Day();
            j1 = curriculum.temp_arrange2.position_Of_Hour();
            teacher.t_table[5 * i1 + j1] += curriculum.temp_arrange2.week;
        }
        teacher.penalty += teacher.delta[teacher.curIndex];
    }

    public void updateGroupPenalty(Curriculum curriculum) {
        for (int u = 0; u < curriculum.groups.size(); u++) {
            Group group = curriculum.groups.get(u);
            group.curIndex = group.cur.indexOf(curriculum);
            for (int i = 0; i < curriculum.arranges.size(); i++) {
                Arrange arrange1 = curriculum.arranges.get(i);
                int i1 = arrange1.position_Of_Day();
                int j1 = arrange1.position_Of_Hour();
                group.t_table[5 * i1 + j1] -= arrange1.week;
            }
            int i1 = curriculum.temp_arrange1.position_Of_Day();
            int j1 = curriculum.temp_arrange1.position_Of_Hour();
            group.t_table[5 * i1 + j1] += curriculum.temp_arrange1.week;
            if (curriculum.cur_times > 24) {
                i1 = curriculum.temp_arrange2.position_Of_Day();
                j1 = curriculum.temp_arrange2.position_Of_Hour();
                group.t_table[5 * i1 + j1] += curriculum.temp_arrange2.week;
            }
            group.penalty += group.delta[group.curIndex];
        }
    }

    public void makeMoveOne(Solver solver, Curriculum curriculum) {
        curriculum.make_Taboo(solver);
        updateTeacherPenalty(curriculum);
        updateGroupPenalty(curriculum);
        curriculum.arranges.set(0, curriculum.temp_arrange1);
        if (curriculum.cur_times > 24) {
            curriculum.arranges.set(1, curriculum.temp_arrange2);
        }
    }

    public void updateGroupPenaltyWeek(Curriculum curriculum) {
        for (int p = 0; p < curriculum.groups.size(); p++) {
            Group group = curriculum.groups.get(p);
            group.curIndex = group.cur.indexOf(curriculum);
            for (int i = 0; i < curriculum.arranges.size(); i++) {
                Arrange arrange1 = curriculum.arranges.get(i);
                int i1 = arrange1.position_Of_Day();
                int j1 = arrange1.position_Of_Hour();
                group.t_table[5 * i1 + j1] -= curriculum.arranges.get(i).week;
                group.t_table[5 * i1 + j1] += curriculum.temp_week.get(i);
            }
            group.penalty += group.delta[group.curIndex];
        }
    }

    public void updateTeacherPenaltyWeek(Curriculum curriculum) {
        Teacher teacher = curriculum.teacher;
        teacher.curIndex = teacher.cur.indexOf(curriculum);
        for (int i = 0; i < curriculum.arranges.size(); i++) {
            Arrange arrange1 = curriculum.arranges.get(i);
            int i1 = arrange1.position_Of_Day();
            int j1 = arrange1.position_Of_Hour();
            teacher.t_table[5 * i1 + j1] -= curriculum.arranges.get(i).week;
            teacher.t_table[5 * i1 + j1] += curriculum.temp_week.get(i);
        }
        teacher.penalty += teacher.delta[teacher.curIndex];
    }

    public void makeMoveTwo(Solver solver, Curriculum curriculum) {
        curriculum.make_Taboo_Week(solver);
        updateTeacherPenaltyWeek(curriculum);
        updateGroupPenaltyWeek(curriculum);
        curriculum.arranges.get(0).week = curriculum.temp_week.get(0);
        if (curriculum.cur_times > 24) {
            curriculum.arranges.get(1).week = curriculum.temp_week.get(1);
        }
    }

    public void taboo_Search(Solver solver) {
        iterator = 0;         //改变节次
        init_Taboo();
        for (int i = 0; i < c.size(); i++) {
            c.get(i).makeAlternativeWeek();
        }
        for (int i = 0; i < c.size(); i++) {
            c.get(i).neighborhoodAction(solver);
        }
        Curriculum curriculum = new Curriculum();
        while (iterator < 0) {
            if (iterator > 0) {
                updateCurriculumDelta(solver, curriculum);
            }
            int j = find_Move();
            curriculum = c.get(j);
            if(curriculum.delta<0){
                bestTime=System.currentTimeMillis()-solver.startTime;
                bestIter=iterator;
            }
           // System.out.println(curriculum.delta);
            penalty_value += curriculum.delta;
           // System.out.println(penalty_value);
            //System.out.println(curriculum.task_no + curriculum.cur_name);
            if(curriculum.sign==0){
                makeMoveOne(solver, curriculum);
            }else {
                makeMoveTwo(solver, curriculum);
            }
            iterator++;
        }
    }

    public void init_Taboo() {
        for (int i = 0; i < c.size(); i++) {
            for (int j = 0; j < 20; j++) {
                for (int k = 0; k < 20; k++) {
                    c.get(i).taboo[j][k] = 0;
                    c.get(i).tabooWeek[j][k] = 0;
                }
            }
        }
    }

    public int find_Move() {
        int j = 0;
        int best_count = 1;
        for (int i = 1; i < c.size(); i++) {
            if (c.get(i).delta < c.get(j).delta) {
                j = i;
                best_count = 1;
            }
            if (c.get(i).delta == c.get(j).delta) {
                best_count++;
                if ((int) (Math.random() * best_count) == 0) {
                    j = i;
                }
            }
        }
        return j;
    }

    public void set_Coefficient(int p1, int p2, int p3, int p4) {
        v1 = p1;
        v2 = p2;
        v3 = p3;
        v4 = p4;
    }

    public static void main(String[] args) {
        Solver solver = new Solver();
        solver.startTime=System.currentTimeMillis();
        solver.readFile();
        solver.teacher_Preserved();
        solver.group_Preserved();
        for (int i = 0; i < solver.c.size(); i++) {
            solver.c.get(i).curriculum_Arrangement();
            System.out.println(solver.c.get(i));
        }
        solver.set_Coefficient(1, 1, 1, 1);
      //  System.out.println(solver.count_Conflicts());
        solver.penalty_value = solver.count_Soft_Constraints();
        System.out.println(solver.penalty_value);
        solver.taboo_Search(solver);
       /* for (int i = 0; i < solver.c.size(); i++) {
            System.out.println(solver.c.get(i));
        }*/
      //  System.out.println(solver.penalty_value);
      //  System.out.println("获得最优解时间：" + (solver.bestTime) + "ms");
       // System.out.println("获得最优解迭代次数："+solver.bestIter);
        System.out.println(System.currentTimeMillis()-solver.startTime);
    }
}