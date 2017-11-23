package ielab.bean;

import lombok.Data;

@Data
public class Rounds {

    private int[][] rounds;
    private int participantNumber;

    public Rounds() {

    }

    public Rounds(int participantNumber) {
        setParticipantNumber(participantNumber);
        int[][] roundsMatrix = new int[2 * (participantNumber - 1)][participantNumber];
        int[][] participantMatrix = new int[participantNumber][participantNumber];
        //初始化participantMatrix
        for (int i = 0; i < participantNumber; i++) {
            for (int j = 0; j < participantNumber; j++) {
                if (i == j) {
                    participantMatrix[i][j] = 1;
                }
            }
        }

        this.rounds = matchParticipant(roundsMatrix, participantMatrix);
    }

    public int[][] matchParticipant(int[][] roundsMatrix, int[][] participantMatrix) {

        int[][] roundsMatrix1 = new int[2 * (participantNumber - 1)][participantNumber];
        int[][] participantMatrix1 = new int[participantNumber][participantNumber];

        int curCycle = -1;
        int curCol = -1;
        boolean f = true;
        for (int i = 0; i < 2 * (participantNumber - 1) && f; i++) {
            for (int j = 0; j < participantNumber && f; j++) {
                if (roundsMatrix[i][j] == 0) {
                    curCycle = i;
                    curCol = j;
                    f = false;
                }
            }

        }

        if (curCycle == -1 || curCol == -1) {
            return roundsMatrix;
        }

        //给第i轮分配

        int curParticipant = -1;
        //看匹配几号参与者
        for (int k = 0; k < participantNumber; k++) {
            boolean flag = false;
            for (int m = 0; m < participantNumber; m++) {
                if (roundsMatrix[curCycle][m] == k + 1) {
                    flag = true;
                    break;
                }

            }
            if (!flag) {
                curParticipant = k;
                //搜索第k参与者的匹配矩阵
                for (int i = 0; i < participantNumber; i++) {
                    if (participantMatrix[curParticipant][i] == 0) {

                        boolean f1 = false;
                        //要保证i在次轮中没有出现过
                        for (int m = 0; m < participantNumber; m++) {
                            if (roundsMatrix[curCycle][m] == i + 1) {
                                f1 = true;
                                break;
                            }
                        }
                        if (f1) {
                            continue;
                        }

                        int[][] copyRoundsMatrix = copyTwoDimArray(roundsMatrix);
                        int[][] copyParticipantMatrix = copyTwoDimArray(participantMatrix);

                        //选择curParticipant作为供应商,i作为零售商进行匹配
                        copyParticipantMatrix[curParticipant][i] = 1;
                        copyRoundsMatrix[curCycle][curCol] = curParticipant + 1;
                        copyRoundsMatrix[curCycle][curCol + 1] = i + 1;

                        //进行下一次迭代
                        int[][] res = matchParticipant(copyRoundsMatrix, copyParticipantMatrix);
                        if (res != null) {
                            return res;
                        }
                    }
                }
            }
        }

        return null;

    }

    private int[][] copyTwoDimArray(int[][] roundsMatrix) {

        if (roundsMatrix == null || roundsMatrix.length == 0) {
            return new int[0][0];
        }
        int[][] arr = new int[roundsMatrix.length][roundsMatrix[0].length];

        for (int i = 0; i < roundsMatrix.length; i++) {
            for (int j = 0; j < roundsMatrix[0].length; j++) {
                arr[i][j] = roundsMatrix[i][j];
            }
        }
        return arr;
    }

    public static void main(String[] args) {
        new Rounds(6);
    }
}