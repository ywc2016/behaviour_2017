package ielab.util;

import ielab.dao.BargainExperimentsDao;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;

/**
 * 计算谈判积分
 * <p>
 * 如果合作，制造商的利润为：p*min(min(K-Q,Q^*),d)+w*Q-c*min(Q+Q^*,K)
 * 零售商的利润为：p*min(d,Q)-w*Q；（注意：需要分别计算这个100个d值对应的利润，最后求均值）
 * </p>
 * <p>
 * 如果不合作，制造商的利润为：p*min(d,Q^*)-c*Q^*，
 * 其中Q^*=min(a+(b-a)*(p-c)/p，K)；零售商的利润为0；（注意：需要分别计算这个100个d值对应的利润，最后求均值）
 * </p>
 * <p>
 * <p>
 * 均值:
 * a=min(DemandS);
 * b=max(DemandS);
 * prob=1/(b-a);
 * <p>
 * %% 基本参数
 * K=120;
 * p=40;
 * c=10; %如果是零售商的话，c需要替代为w
 * Qbest=a+(b-a)*(p-c)/p; %供应商为自己市场的最优生产量
 * <p>
 * %报童利润计算
 * w=27; %举例，这个是输入项，合同达成协议价格
 * Q=60; %举例，这个是输入项，合同达成协议数量
 * QS=min(K-Q,Qbest); %供应为自己市场的产量
 * <p>
 * profitS=w*Q+p*QS*(b-QS)/(b-a)+p*(a+QS)*(QS-a)/(2*(b-a))-c*(Q+QS); %供应商的利润
 * <p>
 * profitR=-w*Q+p*Q*(b-Q)/(b-a)+p*(a+Q)*(Q-a)/(2*(b-a)); %零售商的利润
 *
 * @author ywc
 */
public class BargainPointCalculate {

    // private int numberOfRandom = 100;


    /**
     * 达成协议的供应商(第一参与者)
     *
     * @return
     */
    public double calculateAverageAgreeSupplier(double w, int Q, double K, double c, int a, int b, double p,
                                                String randomNeeds) {

        List<String> randomNeedsList = Arrays.asList(randomNeeds.split(","));

        double profits = 0;
        for (int i = 0; i < randomNeedsList.size(); i++) {
            int d = Integer.parseInt(randomNeedsList.get(i));
            profits += p * min(d, min(K - Q, min(K, a + (b - a) * (p - c) / p))) + w * Q
                    - c * min(K, Q + min(K, a + (b - a) * (p - c) / p));
        }
        return profits / randomNeedsList.size();
    }

    public double calculateAverageAgreeRetailer(double w, int Q, double K, double c, int a, int b, double p,
                                                String randomNeeds) {

        List<String> randomNeedsList = Arrays.asList(randomNeeds.split(","));

        double profits = 0;
        for (int i = 0; i < randomNeedsList.size(); i++) {
            int d = Integer.parseInt(randomNeedsList.get(i));
            profits += p * min(d, Q) - w * Q;
        }
        return profits / randomNeedsList.size();
    }

    /**
     * 未达成协议
     *
     * @param w
     * @param Q
     * @param K
     * @param c
     * @return
     */
    public double calculateAverageDisagreeSupplier(double w, int Q, double K, double c, int a, int b, double p,
                                                   String randomNeeds) {

        List<String> randomNeedsList = Arrays.asList(randomNeeds.split(","));
        double profits = 0;
        for (int i = 0; i < randomNeedsList.size(); i++) {
            int d = Integer.parseInt(randomNeedsList.get(i));
            profits += p * min(d, min(K, a + (b - a) * (p - c) / p))
                    - c * min(K, a + (b - a) * (p - c) / p);
        }
        return profits / randomNeedsList.size();
    }

    public double calculateAverageDisagreeRetailer(double w, int Q, double K, double c, int a, int b, double p,
                                                   String randomNeeds) {
        return 0.0;
    }

    public double calculateOneAgreeSupplier(
            double w, int Q, double K, double c, int a, int b, double p, int d) {


        double profits = 0.0;
        profits = p * min(d, min(K - Q, min(K, a + (b - a) * (p - c) / p))) + w * Q
                - c * min(K, Q + min(K, a + (b - a) * (p - c) / p));
        return profits;

    }


    public double calculateOneAgreeRetailer(
            double w, int Q, double K, double c, int a, int b, double p, int d) {


        double profits = 0.0;
        profits = p * min(d, Q) - w * Q;
        return profits;
    }

    public double calculateOneDisagreeSupplier(
            double w, int Q, double K, double c, int a, int b, double p, int d) {

        double profits = 0.0;
        profits = p * min(d, min(K, a + (b - a) * (p - c) / p))
                - c * min(K, a + (b - a) * (p - c) / p);
        return profits;
    }

    public double calculateOneDisagreeRetailer(
            double w, int Q, double K, double c, int a, int b, double p, int d) {
        return 0.0;
    }


    public double calculateExpectedAgreeSupplier(
            double w, int Q, double K, double c, int a, int b, double p) {

        double Qbest = a + (b - a) * (p - c) / p; //供应商为自己市场的最优生产量
        double QS = Math.min(K - Q, Qbest); //供应为自己市场的产量
        double profitS = w * Q + p * QS * (b - QS) / (b - a) + p * (a + QS) * (QS - a) / (2 * (b - a)) - c * (Q + QS); //供应商的利润
        return profitS;
    }

    public double calculateExpectedAgreeRetailer(
            double w, int Q, double K, double c, int a, int b, double p) {
        double Qbest = a + (b - a) * (p - c) / p; //供应商为自己市场的最优生产量
//        double QS = Math.min(K - Q, Qbest); //供应为自己市场的产量
        double profitR = -w * Q + p * Q * (b - Q) / (b - a) + p * (a + Q) * (Q - a) / (2 * (b - a)); //零售商的利润
        return profitR;
    }

    public double calculateExpectedDisagreeSupplier(
            double w, int Q1, double K, double c, int a, int b, double p) {
        //        double Qbest = a + (b - a) * (p - c) / p; //供应商为自己市场的最优生产量
//        double QS = Math.min(K - Q, Qbest); //供应为自己市场的产量
        double Q = 87.5;
        double profit = -c * Q + p * Q * (b - Q) / (b - a) + p * (a + Q) * (Q - a) / (2 * (b - a));
        return profit;
    }

    public double calculateExpectedDisagreeRetailer(
            double w, int Q1, double K, double c, int a, int b, double p) {
        return 0.0;
    }

    public static void main(String[] args) {

        double w = 22;
        int Q = 60;

        BargainPointCalculate bargainPointCalculate = new BargainPointCalculate();
        BargainExperimentsDao bargainExperimentsDao = new BargainExperimentsDao();
//        BargainExperiments bargainExperiments = bargainExperimentsDao.findByKey(15);
//        double result1 = bargainPointCalculate.calculateAverageAgreeSupplier(w, Q, 120, 10, 50, 100, 40,
//                bargainExperiments.getRandomNeed());
//        System.out.println(result1);
//        double result2 = bargainPointCalculate.calculateAverageAgreeRetailer(w, Q, 120, 10, 50, 100, 40,
//                bargainExperiments.getRandomNeed());
//        System.out.println(result2);

        double d = bargainPointCalculate.calculateOneDisagreeSupplier(0.0, 0, 120, 10, 50, 100, 40, 96);
        System.out.println(d);
    }
}
