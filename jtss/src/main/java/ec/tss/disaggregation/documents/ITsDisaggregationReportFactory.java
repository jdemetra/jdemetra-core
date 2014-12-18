/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tss.disaggregation.documents;

import java.util.Map;

/**
 *
 * @author Jean
 */
public interface ITsDisaggregationReportFactory {
    String getReportName();
    String getReportDescription();
    boolean createReport(Map<String, TsDisaggregationModelDocument> processing);
}
