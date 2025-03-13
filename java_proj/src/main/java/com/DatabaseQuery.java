package com;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides methods to query data from the database, including twosides and drug names
 */
public class DatabaseQuery implements Queryable {
    private static final Logger logger = Logger.getLogger(DatabaseQuery.class.getName());
    private static String url;
    private static String username;
    private static String password;

    /**
     * Establishes an initial connection to the database and logs the connection status
     */
    public static void connectInitially(String lurl, String user, String pass) {
        url = lurl;
        username = user;
        password = pass;
        try (Connection connection=DatabaseConnection.connect(url, user, password)) {
            if (connection!=null) {
                logger.info("Connected to the database successfully");
            } else {
                logger.warning("Failed to connect to the database");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while interacting with the database", e);
        }
    }

    /**
     * Queries the database for twosides
     *
     * @param drug1Name the name of the first drug
     * @param drug2Name the name of the second drug
     * @param filtered whether to exclude interactions with unknown or negligible severity
     * @return a TwosidesCol object containing the queried drug interactions
     */
    public TwosidesCol queryTwosides(String drug1Name, String drug2Name, boolean filtered) {
        TwosidesCol twosidesCol = new TwosidesCol();
        Map<String, Twosides> twosidesInteractionMap = new HashMap<>();
        try (Connection connection = DatabaseConnection.connect(url, username, password)) {
            if (connection != null) {
                logger.info("Connected to the database successfully");
                String query;
                if (filtered) {                
                    query = "SELECT * FROM effect_nsides.twosides WHERE ((drug_1_concept_name = ? AND drug_2_concept_name = ?) OR (drug_1_concept_name = ? AND drug_2_concept_name = ?)) AND severity_class != 'Unknown' AND severity_class !='Negligible'";
                }
                else {
                    query = "SELECT * FROM effect_nsides.twosides WHERE (drug_1_concept_name = ? AND drug_2_concept_name = ?) OR (drug_1_concept_name = ? AND drug_2_concept_name = ?)";
                }
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, drug1Name);
                preparedStatement.setString(2, drug2Name);
                preparedStatement.setString(3, drug2Name);
                preparedStatement.setString(4, drug1Name);

                logger.log(Level.INFO, "Executing query: {0}", query);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    // logger.log(Level.INFO, "Interaction found: {0}", resultSet.getString("condition_name"));
                    Twosides tempTwosides = new Twosides(resultSet.getInt("drug_1_rxnorm_id"), 
                                                        resultSet.getString("drug_1_concept_name"),
                                                        resultSet.getInt("drug_2_rxnorm_id"), 
                                                        resultSet.getString("drug_2_concept_name"),
                                                        resultSet.getInt("condition_meddra_id"), 
                                                        resultSet.getString("condition_name"),
                                                        resultSet.getInt("a"), 
                                                        resultSet.getInt("b"), 
                                                        resultSet.getInt("c"), 
                                                        resultSet.getInt("d"),
                                                        resultSet.getDouble("prr"), 
                                                        resultSet.getDouble("prr_error"), 
                                                        resultSet.getDouble("mean_reporting_frequency"),
                                                        resultSet.getDouble("severity"),
                                                        resultSet.getString("severity_class")
                                                        );
                    twosidesInteractionMap.put(TwosidesCol.getMapStrId(tempTwosides), tempTwosides);
                }
                twosidesCol.addTwosides(TwosidesCol.getMapStrId(drug1Name, drug2Name), twosidesInteractionMap);
            } else {
                logger.warning("Failed to connect to the database");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while interacting with the database", e);
        }
        return twosidesCol;
    }

    /**
     * Searches for drugs in the database that match the given name
     *
     * @param drugName the name of the drug to search for
     * @param like whether to use a wildcard search or an exact match
     * @return a list of drug names matching the search criteria
     */
    public List<String> queryDrug(String drugName, boolean like) {
        List<String> results = new ArrayList<>();
        try (Connection connection = DatabaseConnection.connect(url, username, password)) {
            if (connection!=null) {
                logger.info("Connected to the database successfully");
                String query;
                if (like) {
                    query = "SELECT DISTINCT drug_1_concept_name FROM effect_nsides.twosides WHERE drug_1_concept_name LIKE ? LIMIT 50";
                }
                else {
                    query = "SELECT DISTINCT drug_1_concept_name FROM effect_nsides.twosides WHERE drug_1_concept_name = ? LIMIT 1";
                }
                PreparedStatement stmt = connection.prepareStatement(query);
                if (like) {
                    stmt.setString(1, "%" + drugName + "%");
                }
                else {
                    stmt.setString(1, drugName);
                }
                ResultSet rs = stmt.executeQuery();
        
                while (rs.next()) {
                    // logger.log(Level.INFO, "Drug found: {0}", resultSet.getString("drug_1_concept_name"));
                    results.add(rs.getString("drug_1_concept_name"));
                    }
            }
            else {
                logger.warning("Failed to connect to the database");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while interacting with the database", e);
        }
        return results;
    }
}
