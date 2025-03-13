package com;
import java.util.HashMap;
import java.util.Map;

public class mockDb {
//mock db for testing in case mysql decides to play truant again
    public static final TwosidesCol getDb() {
        TwosidesCol twosidesCol = new TwosidesCol();

        Map<String, Twosides> aspirinIbuprofenMap = new HashMap<>();
        aspirinIbuprofenMap.put("Aspirin-Ibuprofen-Asthenia", new Twosides(1191, "Aspirin", 5640, "Ibuprofen", 10003549, "Asthenia", 269, 6715, 1444, 68396, 1.86288, 0.0652111, 0.0385166, 1.1003, "Medium"));
        aspirinIbuprofenMap.put("Aspirin-Ibuprofen-Myocardial_infarction", new Twosides(1191, "Aspirin", 5640, "Ibuprofen", 10028596, "Myocardial infarction", 304, 6680, 1415, 68425, 2.14841, 0.0619571, 0.0435281, 1.50937, "Medium"));
        aspirinIbuprofenMap.put("Aspirin-Ibuprofen-Gastrointestinal_haemorrhage", new Twosides(1191, "Aspirin", 5640, "Ibuprofen", 10017955, "Gastrointestinal haemorrhage", 231, 6753, 382, 69458, 6.04712, 0.0823972, 0.0330756, 2.42741, "Medium"));
        aspirinIbuprofenMap.put("Aspirin-Ibuprofen-Anxiety", new Twosides(1191, "Aspirin", 5640, "Ibuprofen", 10002855, "Anxiety", 317, 6667, 1228, 68612, 2.58143, 0.0617366, 0.0453895, 1.8979, "Medium"));
        aspirinIbuprofenMap.put("Aspirin-Ibuprofen-Nausea", new Twosides(1191, "Aspirin", 5640, "Ibuprofen", 10028813, "Nausea", 469, 6515, 2952, 66888, 1.58875, 0.0480983, 0.0671535, 2.21817, "Medium"));
        aspirinIbuprofenMap.put("Aspirin-Ibuprofen-Coronary_artery_disease", new Twosides(1191, "Aspirin", 5640, "Ibuprofen", 10011078, "Coronary artery disease", 154, 6830, 261, 69579, 5.90038, 0.100834, 0.0220504, 1.2903, "Medium"));
        aspirinIbuprofenMap.put("Aspirin-Ibuprofen-Flushing", new Twosides(1191, "Aspirin", 5640, "Ibuprofen", 10016825, "Flushing", 242, 6742, 642, 69198, 3.76947, 0.0743798, 0.0346506, 1.75605, "Medium"));
        aspirinIbuprofenMap.put("Aspirin-Ibuprofen-Pain_in_extremity", new Twosides(1191, "Aspirin", 5640, "Ibuprofen", 10033425, "Pain in extremity", 273, 6711, 1116, 68724, 2.44624, 0.0663442, 0.0390893, 1.4413, "Medium"));
        aspirinIbuprofenMap.put("Aspirin-Ibuprofen-Dizziness", new Twosides(1191, "Aspirin", 5640, "Ibuprofen", 10013573, "Dizziness", 404, 6580, 1933, 67907, 2.09002, 0.0532454, 0.0578465, 2.27063, "Medium"));
        aspirinIbuprofenMap.put("Aspirin-Ibuprofen-Depression", new Twosides(1191, "Aspirin", 5640, "Ibuprofen", 10012378, "Depression", 287, 6697, 1195, 68645, 2.40167, 0.0645263, 0.0410939, 1.52952, "Medium"));

        twosidesCol.addTwosides("Aspirin%Ibuprofen", aspirinIbuprofenMap);

        Map<String, Twosides> temazepamSildenafilMap = new HashMap<>();
        temazepamSildenafilMap.put("Temazepam-sildenafil-Fatigue", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10016256, "Fatigue", 19, 137, 54, 1506, 3.51852, 0.253177, 0.121795, 1.69264, "Medium"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Dyspnoea", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10013968, "Dyspnoea", 20, 136, 46, 1514, 4.34783, 0.254338, 0.128205, 2.19163, "Medium"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Dizziness", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10013573, "Dizziness", 18, 138, 49, 1511, 3.67347, 0.262512, 0.115385, 1.61464, "Medium"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Back_pain", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10003988, "Back pain", 11, 145, 16, 1544, 6.875, 0.382567, 0.0705128, 1.26716, "Medium"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Asthenia", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10003549, "Asthenia", 11, 145, 27, 1533, 4.07407, 0.347699, 0.0705128, 0.826215, "Minor"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Fall", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10016173, "Fall", 10, 146, 27, 1533, 3.7037, 0.360535, 0.0641026, 0.658512, "Minor"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Pyrexia", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10037660, "Pyrexia", 10, 146, 37, 1523, 2.7027, 0.346375, 0.0641026, 0.500181, "Minor"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Insomnia", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10022437, "Insomnia", 11, 145, 24, 1536, 4.58333, 0.354294, 0.0705128, 0.91219, "Minor"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Weight_decreased", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10047895, "Weight decreased", 10, 146, 13, 1547, 7.69231, 0.412155, 0.0641026, 1.19639, "Medium"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Hypotension", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10021097, "Hypotension", 12, 144, 17, 1543, 7.05882, 0.367567, 0.0769231, 1.47724, "Medium"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Headache", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10019211, "Headache", 16, 140, 41, 1519, 3.90244, 0.282558, 0.102564, 1.41652, "Medium"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Injury", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10022116, "Injury", 12, 144, 13, 1547, 9.23077, 0.391414, 0.0769231, 1.81409, "Medium"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Malaise", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10025482, "Malaise", 12, 144, 41, 1519, 2.92683, 0.317289, 0.0769231, 0.709577, "Minor"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Anxiety", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10002855, "Anxiety", 15, 141, 34, 1526, 4.41176, 0.298374, 0.0961538, 1.42173, "Medium"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Erectile_dysfunction", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10061461, "Erectile dysfunction", 11, 145, 4, 1556, 27.5, 0.577804, 0.0705128, 3.35599, "Medium"));
        temazepamSildenafilMap.put("Temazepam-sildenafil-Depression", new Twosides(10355, "Temazepam", 136411, "sildenafil", 10012378, "Depression", 12, 144, 30, 1530, 4.0, 0.331082, 0.0769231, 0.929354, "Minor"));

        twosidesCol.addTwosides("Temazepam%Sildenafil", temazepamSildenafilMap);

        return twosidesCol;
    }
}

