package ielab.dao;

import ielab.bean.Rounds;
import ielab.hibernate.BargainExperiments;
import ielab.hibernate.BargainMatch;
import ielab.hibernate.BargainParameter;
import ielab.hibernate.BargainParticipant;
import lombok.NonNull;
import org.hibernate.Query;
import org.hibernate.Session;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class BargainMatchDao extends BaseDao<BargainMatch> {


    private BargainParticipantDao bargainParticipantDao = new BargainParticipantDao();
    private BargainParameterDao bargainParameterDao = new BargainParameterDao();
    private BargainExperimentsDao bargainExperimentsDao = new BargainExperimentsDao();

    public static void main(String[] args) {
        // new BargainMatchDao().initBargainMatch(1);
        // BargainMatch bargainMatch = new BargainMatchDao().findMatch(1, 1);
        // System.out.println(bargainMatch.getId());
        System.out.println(new BargainMatchDao().isParticipantFinishAllMatch(20, 4));
    }

    /**
     * 初始化谈判匹配
     *
     * @param experimentId   实验id
     * @param numberOfRandom 随机需求的个数
     */
    public void initBargainMatch(int experimentId, int numberOfRandom) {

        BargainExperiments bargainExperiments = bargainExperimentsDao.findByKey(experimentId);
        if (bargainExperiments == null) {
            return;
        }

        BargainParameter bargainParameter = bargainExperimentsDao.findBargainParameterByExperimentId(experimentId);
        int numOfParticipants = bargainParameter.getNumberOfPerson();

        // 初始化参与者信息,写入数据库

        for (int i = 0; i < numOfParticipants; i++) {
            BargainParticipant bargainParticipant = new BargainParticipant();
            bargainParticipant.setExperimentId(experimentId);
            bargainParticipant.setNumber(i + 1);// 设置编号
            bargainParticipant.setStatus("空闲中");
            bargainParticipant.setCurrentCycle(0);
            bargainParticipantDao.save(bargainParticipant);
        }

        List<BargainParticipant> bargainParticipants = bargainParticipantDao.findByPropertyEqual("experimentId",
                experimentId + "", "int");

        // 将匹配分组信息写入数据库,并加上轮数

        Rounds rounds = new Rounds(bargainParticipants.size());
        int[][] rounds_ = rounds.getRounds();
        replaceNumberWithParticipantId(rounds_, experimentId);

        int a = bargainParameter.getA();
        int b = bargainParameter.getB();

        for (int i = 0; i < rounds_.length; i++) {
            for (int j = 0; j < numOfParticipants; j += 2) {

                String status = "未完成";
                BargainMatch bargainMatch = new BargainMatch();
                bargainMatch.setExperimentId(experimentId);
                bargainMatch.setParticipantId(rounds_[i][j]);
                bargainMatch.setSecondParticipantId(rounds_[i][j + 1]);
                bargainMatch.setStatus(status);
                bargainMatch.setParticipantStatus("未开始");
                bargainMatch.setSecomdParticipantStatus("未开始");
                bargainMatch.setCycle(i + 1);
                bargainMatch.setSupplierDemand((int) (Math.random() * (b - a)) + a);
                bargainMatch.setRetailerDemand((int) (Math.random() * (b - a)) + a);
                this.save(bargainMatch);
            }
        }

        // 生成随机需求,写入数据库

        StringBuilder randomNeed = new StringBuilder();
        for (int i = 0; i < numberOfRandom; i++) {
            int d = (int) (Math.random() * (b - a)) + a;
            randomNeed.append(d + ",");
        }
        randomNeed.deleteCharAt(randomNeed.length() - 1);// 删除最后一个,
        bargainExperiments.setRandomNeed(randomNeed.toString());
        bargainExperimentsDao.update(bargainExperiments);


    }

    private void replaceNumberWithParticipantId(int[][] rounds_, int experimentId) {
        BargainExperiments bargainExperiments = bargainExperimentsDao.findByKey(experimentId);
        if (bargainExperiments == null) {
            return;
        }
        @NonNull List<BargainParticipant> bargainParticipants = bargainParticipantDao
                .findByPropertyEqual("experimentId",
                        experimentId + "", "" +
                                "long");
        int[] ids = new int[bargainParticipants.size()];
        for (BargainParticipant bargainParticipant : bargainParticipants) {
            ids[bargainParticipant.getNumber() - 1] = bargainParticipant.getId();
        }

        for (int i = 0; i < rounds_.length; i++) {
            for (int j = 0; j < rounds_[0].length; j++) {
                rounds_[i][j] = ids[rounds_[i][j] - 1];
            }
        }
    }

    /**
     * 根据参与者id和实验id返回匹配结果,如果结束则返回null
     *
     * @param participantId
     * @return
     */
    public BargainMatch findMatch(int participantId, int experimentId) {


        BargainExperiments bargainExperiments = bargainExperimentsDao.findByKey(experimentId);
        BargainParameter bargainParameter = bargainParameterDao.findByKey(bargainExperiments.getParId());
        BargainParticipant bargainParticipant = bargainParticipantDao.findByKey(participantId);

        try {
            String queryString = "from BargainMatch  as a ,BargainParticipant as b,BargainParticipant as c "
                    + " where a.participantId = b.id and a.experimentId = b.experimentId and "
                    + " a.secondParticipantId = c.id and a.experimentId = c.experimentId"
                    + " and b.status = '空闲中' and c.status = '空闲中'"
                    + " and a.experimentId = :experimentId and (a.participantId = :participantId ) " +
                    "and a.status = '未完成' and a.cycle= :cycle";
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery(queryString);
            query.setInteger("experimentId", experimentId);
            query.setInteger("participantId", participantId);
            query.setInteger("cycle", bargainParticipant.getCurrentCycle() + 1);

            List pojos = query.list();
            session.getTransaction().commit();

            if (pojos.size() == 0) {
                return null;
            }
            Iterator iterator = pojos.iterator();

            if (iterator.hasNext()) {
                Object[] objects = (Object[]) iterator.next();
                BargainMatch bargainMatch = (BargainMatch) objects[0];
                BargainParticipant bargainParticipant1 = (BargainParticipant) objects[1];
                BargainParticipant bargainParticipant2 = (BargainParticipant) objects[2];

//                  设置用户的状态
                if (bargainParticipant1.getStatus().equals("空闲中")) {
                    bargainParticipant1.setStatus("谈判中");
                }
                if (bargainParticipant1.getCurrentCycle() < bargainExperiments.getCurrentCycle()) {
                    bargainParticipant1.setCurrentCycle(bargainParticipant1.getCurrentCycle() + 1);//周期往后推1
                }

                if (bargainParticipant2.getStatus().equals("空闲中")) {
                    bargainParticipant2.setStatus("谈判中");
                }
                if (bargainParticipant2.getCurrentCycle() < bargainExperiments.getCurrentCycle()) {
                    bargainParticipant2.setCurrentCycle(bargainParticipant2.getCurrentCycle() + 1);//周期往后推1
                }
                if (bargainParticipant1.getMatchId() == null) {

                    bargainParticipant1.setMatchId(bargainMatch.getId());
                }

                if (bargainParticipant2.getMatchId() == null) {

                    bargainParticipant2.setMatchId(bargainMatch.getId());
                }

                bargainParticipantDao.update(bargainParticipant1);
                bargainParticipantDao.update(bargainParticipant2);

                //设置这轮的开始时间
                if (bargainMatch.getBeginTime() == null) {
                    bargainMatch.setBeginTime(new Timestamp(new Date().getTime()));
                }
//                    bargainMatch.setCycle(bargainExperiments.getCurrentCycle());
                this.update(bargainMatch);
                return bargainMatch;

            }
//                Iterator iterator = pojos.iterator();
//                for (; iterator.hasNext(); ) {
//                    Object[] objects = (Object[]) iterator.next();
//                    BargainMatch bargainMatch = (BargainMatch) objects[0];
//                    BargainParticipant bargainParticipant1 = (BargainParticipant) objects[1];
//                    BargainParticipant bargainParticipant2 = (BargainParticipant) objects[2];
//
//                    if (bargainParticipant1.getStatus().equals("空闲中")
//                            && bargainParticipant1.getCurrentCycle() < bargainExperiments.getCurrentCycle()
//                            && bargainParticipant2.getStatus().equals("空闲中")
//                            && bargainParticipant2.getCurrentCycle() < bargainExperiments.getCurrentCycle()) {// 符合周期条件
//
//                        //判断是否可以这样匹配,考虑到接下来的用户找不到匹配项
////                        if (!canMatch(bargainMatch, bargainExperiments, new ArrayList<Integer>(), new ArrayList<Integer>())) {
////                            continue;
////                        }
//
//                        // 设置用户的状态
//                        BargainParticipantDao bargainParticipantDao = new BargainParticipantDao();
//                        bargainParticipant1.setStatus("谈判中");
//                        bargainParticipant1.setCurrentCycle(bargainParticipant1.getCurrentCycle() + 1);//周期往后推1
//
//                        bargainParticipant2.setStatus("谈判中");
//                        bargainParticipant2.setCurrentCycle(bargainParticipant2.getCurrentCycle() + 1);//周期往后推1
//
//                        bargainParticipant1.setMatchId(bargainMatch.getId());
//                        bargainParticipant2.setMatchId(bargainMatch.getId());
//                        bargainParticipantDao.update(bargainParticipant1);
//                        bargainParticipantDao.update(bargainParticipant2);
//
//                        //设置这轮的开始时间
//                        bargainMatch.setBeginTime(new Timestamp(new Date().getTime()));
//                        bargainMatch.setCycle(bargainExperiments.getCurrentCycle());
//                        bargainMatchDao.update(bargainMatch);
//
//                        return bargainMatch;
//                    }
//
//                }
//                return null;
        } catch (Exception re) {
            throw re;
        }
        return null;
    }


    private boolean canMatch(BargainMatch bargainMatch, BargainExperiments bargainExperiments,
                             List<Integer> bargainParticipantIdList, List<Integer> bargainMatchIdList) {

        BargainParticipantDao bargainParticipantDao = new BargainParticipantDao();
        BargainMatchDao bargainMatchDao = new BargainMatchDao();

        BargainParticipant bargainParticipant1 = bargainParticipantDao.findByKey(bargainMatch.getParticipantId());
        BargainParticipant bargainParticipant2 = bargainParticipantDao.findByKey(bargainMatch.getSecondParticipantId());

        try {
//            bargainParticipant1.setStatus("谈判中");
//            bargainParticipant1.setCurrentCycle(bargainParticipant1.getCurrentCycle() + 1);//周期往后推1
//
//            bargainParticipant2.setStatus("谈判中");
//            bargainParticipant2.setCurrentCycle(bargainParticipant2.getCurrentCycle() + 1);//周期往后推1
//
//            bargainParticipant1.setMatchId(bargainMatch.getId());
//            bargainParticipant2.setMatchId(bargainMatch.getId());
//            bargainParticipantDao.update(bargainParticipant1);
//            bargainParticipantDao.update(bargainParticipant2);
//
//            //设置这轮的开始时间
//            bargainMatch.setBeginTime(new Timestamp(new Date().getTime()));
//            bargainMatch.setCycle(bargainExperiments.getCurrentCycle());
//            bargainMatchDao.update(bargainMatch);


            //搜索剩下的空闲参与者,挑选一个
            BargainParticipant lazyBargainParticipant = findLazyParticipant(bargainExperiments, bargainParticipantIdList);


            if (lazyBargainParticipant == null) {
                return true;
            } else {
                List<BargainMatch> bargainMatchList2 = findBargainMatchesByParticipant(bargainExperiments, lazyBargainParticipant, bargainMatchIdList);
                if (bargainMatchList2 == null || bargainMatchList2.size() == 0) {
                    return false;
                } else {
                    bargainMatchIdList.add(bargainMatch.getId());
                    bargainParticipantIdList.add(bargainParticipant1.getId());
                    bargainParticipantIdList.add(bargainParticipant2.getId());

                    for (BargainMatch bargainMatch1 : bargainMatchList2) {
                        if (canMatch(bargainMatch1, bargainExperiments, copyBargainParticipantIdList(bargainParticipantIdList), copyBargainMatchIdList(bargainMatchIdList))) {
                            return true;
                        }
                    }
                }
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            //还原状态
//            bargainParticipant1.setStatus("空闲中");
//            bargainParticipant1.setCurrentCycle(bargainParticipant1.getCurrentCycle() - 1);//周期往后推1
//
//            bargainParticipant2.setStatus("空闲中");
//            bargainParticipant2.setCurrentCycle(bargainParticipant2.getCurrentCycle() - 1);//周期往后推1
//
//            bargainParticipant1.setMatchId(null);
//            bargainParticipant2.setMatchId(null);
//
//            bargainParticipantDao.update(bargainParticipant1);
//            bargainParticipantDao.update(bargainParticipant2);
//
//            bargainMatch.setBeginTime(null);
//            bargainMatch.setCycle(null);
//            bargainMatchDao.update(bargainMatch);
        }


        return false;
    }

    private List<Integer> copyBargainMatchIdList(List<Integer> bargainMatchIdList) {
        List<Integer> bargainMatchIds = new ArrayList<>();
        for (int i = 0; i < bargainMatchIdList.size(); i++) {
            bargainMatchIds.add(new Integer(bargainMatchIdList.get(i)));
        }
        return bargainMatchIds;
    }

    private List<Integer> copyBargainParticipantIdList(List<Integer> bargainParticipantIdList) {
        List<Integer> bargainParticipantsIds = new ArrayList<>();
        for (Integer bargainParticipantId : bargainParticipantIdList) {
            bargainParticipantsIds.add(new Integer(bargainParticipantId));
        }
        return bargainParticipantsIds;
    }

    private List<BargainMatch> findBargainMatchesByParticipant(
            BargainExperiments bargainExperiments,
            BargainParticipant lazyBargainParticipant, List<Integer> bargainMatchIdList) {
        List<BargainMatch> list = new ArrayList<>();
        try {
            String queryString = "from " + typeClass().getCanonicalName()
                    + " as a ,BargainParticipant as b,BargainParticipant as c "
                    + " where a.participantId = b.id and a.experimentId = b.experimentId and "
                    + " a.secondParticipantId = c.id and a.experimentId = c.experimentId"
                    + " and b.status = '空闲中' and c.status = '空闲中'"
                    + " and a.experimentId = :experimentId and a.participantId = :participantId and a.status = '未完成'";
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery(queryString);
            query.setInteger("experimentId", bargainExperiments.getId());
            query.setInteger("participantId", lazyBargainParticipant.getId());
            List pojos = query.list();
            session.getTransaction().commit();

            if (pojos.size() == 0) {
                return list;
            }


            Iterator iterator = pojos.iterator();
            for (; iterator.hasNext(); ) {
                Object[] objects = (Object[]) iterator.next();
                BargainMatch bargainMatch = (BargainMatch) objects[0];
                BargainParticipant bargainParticipant1 = (BargainParticipant) objects[1];
                BargainParticipant bargainParticipant2 = (BargainParticipant) objects[2];

                if (bargainParticipant1.getStatus().equals("空闲中")
                        && bargainParticipant1.getCurrentCycle() < bargainExperiments.getCurrentCycle()
                        && bargainParticipant2.getStatus().equals("空闲中")
                        && bargainParticipant2.getCurrentCycle() < bargainExperiments.getCurrentCycle()) {// 符合周期条件
                    boolean flag = true;
                    for (Integer bargainMatchId : bargainMatchIdList) {
                        if (bargainMatchId.equals(bargainMatch.getId())) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        list.add(bargainMatch);
                    }
                }
            }
            return list;
        } catch (RuntimeException re) {
            throw re;
        }
    }


    /**
     * 寻找空闲的参与者
     *
     * @param bargainExperiments
     * @return
     */
    private BargainParticipant findLazyParticipant(BargainExperiments bargainExperiments, List<Integer> bargainParticipantIdList) {
        String queryString = "from BargainParticipant as a  where a.experimentId = :experimentId and a.status='空闲中'";
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery(queryString);
        query.setInteger("experimentId", bargainExperiments.getId());
        List<BargainParticipant> pojos = query.list();
        session.getTransaction().commit();
        if (pojos.size() != 0) {
            for (BargainParticipant bargainParticipant : pojos) {
                for (Integer bargainParticipantId : bargainParticipantIdList) {
                    if (bargainParticipantId.equals(bargainParticipant.getId())) {
                        continue;
                    }
                }
                return bargainParticipant;
            }
        }
        return null;
    }

    /**
     * 判断某个参与者是否已经结束了所有的谈判配对
     *
     * @param participantId
     * @param experimentId
     * @return
     */
    public boolean isParticipantFinishAllMatch(int participantId, int experimentId) {

        BargainParticipantDao bargainParticipantDao = new BargainParticipantDao();
        BargainParticipant bargainParticipant = bargainParticipantDao.findByKey(participantId);

        try {
            String queryString = "from " + typeClass().getCanonicalName() + " where experimentId = " + experimentId
                    + " and status = '未完成' and (participantId = " + participantId + "or secondParticipantId = "
                    + participantId + ")";

            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery(queryString);
            List pojos = query.list();
            session.getTransaction().commit();
            if (pojos.size() == 0) {
                return true;
            }
        } catch (RuntimeException re) {
            throw re;
        }

        return false;
    }

    /**
     * 分页查询实验数据,只显示匹配信息
     *
     * @param experimentId
     * @param order
     * @param page
     * @param rows
     * @return
     */
    public List findMatchForpagination(int experimentId, int participantId, String order, String page,
                                       String rows) {
        order = ielab.util.StringUtils.isEmpty(order) ? "id" : order;
        int intPage = ielab.util.StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        int intRows = ielab.util.StringUtils.isEmpty(rows) ? 20 : Integer.parseInt(rows);

        try {

            // String queryString = experimentId == 0 ? "from " +
            // typeClass().getCanonicalName()
            // + " as a, BargainParticipant as b , BargainParticipant as c where
            // a.experimentId <> " + experimentId
            // + " and a.participantId = b.id and a.secondParticipantId = c.id
            // order by a." + order + " asc"
            // : "from " + typeClass().getCanonicalName()
            // + " as a, BargainParticipant as b , BargainParticipant as c where
            // a.experimentId = "
            // + experimentId + " and a.participantId = b.id and
            // a.secondParticipantId = c.id order by a."
            // + order + " asc";

            String queryString = "from BargainMatch"
                    + " as a, BargainParticipant as b , BargainParticipant as c where a.participantId = b.id "
                    + "and a.secondParticipantId = c.id ";

            if (experimentId != 0) {
                queryString += " and a.experimentId = " + experimentId;
            }

            if (participantId != 0) {
                queryString += " and (a.participantId = " + participantId + " or a.secondParticipantId ="
                        + participantId + " )";
            }

            queryString += " order by a." + order + " asc";

            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery(queryString);
            query.setFirstResult((intPage - 1) * intRows);
            query.setMaxResults(intRows);

            List pojos = query.list();
            session.getTransaction().commit();
            return pojos;
        } catch (RuntimeException re) {
            throw re;
        }
    }

    /**
     * 查询匹配总数
     *
     * @param experimentId
     * @return
     */
    public long countRowsByParameter(int experimentId) {

        try {
            String queryString = experimentId == 0 ? "select count(*) from " + typeClass().getCanonicalName()
                    : "select count(*) from " + typeClass().getCanonicalName() + " where experimentId = "
                    + experimentId;

            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery(queryString);

            List pojos = query.list();
            session.getTransaction().commit();
            return (long) pojos.get(0);
        } catch (RuntimeException re) {
            throw re;
        }
    }

    /**
     * 修正匹配中另一个参与者未匹配上的情况
     *
     * @param participantId
     * @param experimentId
     */
    public void refine(int participantId, int experimentId) {
        BargainParticipant bargainParticipant = bargainParticipantDao.findByKey(participantId);
        BargainExperiments bargainExperiments = bargainExperimentsDao.findByKey(experimentId);

        if (bargainParticipant.getCurrentCycle() < bargainExperiments.getCurrentCycle()) {
            String queryString = "from BargainMatch  as a ,BargainParticipant as b,BargainParticipant as c "
                    + " where a.participantId = b.id and a.experimentId = b.experimentId and "
                    + " a.secondParticipantId = c.id and a.experimentId = c.experimentId"
                    + " and a.experimentId = :experimentId and (a.participantId = :participantId or a.secondParticipantId =:participantId) " +
                    "and a.status != '未完成' and a.cycle= :cycle";
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery(queryString);
            query.setInteger("experimentId", experimentId);
            query.setInteger("participantId", participantId);
            query.setInteger("cycle", bargainParticipant.getCurrentCycle() + 1);

            List pojos = query.list();
            session.getTransaction().commit();

            Iterator iterator = pojos.iterator();

            if (iterator.hasNext()) {
                Object[] objects = (Object[]) iterator.next();
                BargainMatch bargainMatch = (BargainMatch) objects[0];
                BargainParticipant bargainParticipant1 = (BargainParticipant) objects[1];
                BargainParticipant bargainParticipant2 = (BargainParticipant) objects[2];

                bargainParticipant.setMatchId(bargainMatch.getId());
                bargainParticipant.setStatus("谈判中");
                bargainParticipant.setCurrentCycle(bargainExperiments.getCurrentCycle());

                bargainParticipantDao.update(bargainParticipant);
            }
        }
    }

}
