package svc.data.municipal;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import svc.data.jdbc.BaseJdbcDao;
import svc.logging.LogSystem;
import svc.models.Court;
import svc.models.Municipality;
import svc.types.HashableEntity;

@Repository
public class MunicipalityDAO extends BaseJdbcDao {

	public static final String MUNICIPALITY_ID_COLUMN_NAMER = "municipality_id";
    public static final String MUNICIPALITY_NAME_COLUMN_NAMER = "municipality_name";
    public static final String MUNICIPALITY_PAYMENT_URL_COLUMN_NAMER = "payment_url";

	public List<Municipality> getByCourtId(Long courtId){
		try{
			Map<String, Object> parameterMap = new HashMap<>();
			parameterMap.put("courtId", courtId);
			String sql = getSql("municipality/get-all.sql") + " WHERE mc.court_id = :courtId ORDER BY m.municipality_name";

            MunicipalityRowCallbackHandler rowCallbackHandler = new MunicipalityRowCallbackHandler();
			jdbcTemplate.query(sql, parameterMap, rowCallbackHandler);

			return rowCallbackHandler.municipalities;
		}catch (Exception e){
            LogSystem.LogDBException(e);
			return null;
		}
	}
	
	public Municipality getByMunicipalityId(Long municipalityId){
		try{
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put("municipalityId", municipalityId);
            String sql = getSql("municipality/get-all.sql") + " WHERE m.municipality_id = :municipalityId";

            MunicipalityRowCallbackHandler rowCallbackHandler = new MunicipalityRowCallbackHandler();
            jdbcTemplate.query(sql, parameterMap, rowCallbackHandler);

            return rowCallbackHandler.municipalities.get(0);
		}catch (Exception e){
            LogSystem.LogDBException(e);
			return null;
		}
	}
	
	public List<Municipality> getAllMunicipalities(Boolean supported){
		try  {
            MunicipalityRowCallbackHandler rowCallbackHandler = new MunicipalityRowCallbackHandler();
            String sql = getSql("municipality/get-all.sql");
            if (supported != null){
            	if (supported){
            		sql += " WHERE cdm.citation_datasource_id IS NOT NULL";
            	}else{
            		sql += " WHERE cdm.citation_datasource_id IS NULL";
            	}
            }
            sql += " ORDER BY m.municipality_name";
            jdbcTemplate.query(sql, rowCallbackHandler);

            return rowCallbackHandler.municipalities;
		} catch (Exception e) {
			LogSystem.LogDBException(e);
			return null;
		}
	}
	
	private final class MunicipalityRowCallbackHandler implements RowCallbackHandler {
	    private Map<Long, Municipality> municipalityMap = new HashMap<>();
	    //use array list so results remain in their sorted order
	    public List<Municipality> municipalities = new ArrayList<Municipality>();
	    
	    @SuppressWarnings("unchecked")
		@Override
        public void processRow(ResultSet rs) {
	    	try {
                Long municipalityId = rs.getLong(MUNICIPALITY_ID_COLUMN_NAMER);
                HashableEntity<Court> courtId = new HashableEntity<Court>(Court.class,rs.getLong(CourtDAO.COURT_ID_COLUMN_NAME));
                if(municipalityMap.containsKey(municipalityId)) {
                    municipalityMap.get(municipalityId).courts.add(courtId);
                } else {
                    Municipality municipality = new Municipality();
                    municipality.id = new HashableEntity<Municipality>(Municipality.class,municipalityId);
                    municipality.name = rs.getString(MUNICIPALITY_NAME_COLUMN_NAMER);
                    municipality.courts = Lists.newArrayList(courtId);
                    municipality.paymentUrl = rs.getString(MUNICIPALITY_PAYMENT_URL_COLUMN_NAMER);
                    municipality.isSupported = rs.getString("citation_datasource_id") != null;

                    municipalityMap.put(municipalityId, municipality);
                    municipalities.add(municipality);
                }
	    	} catch (Exception e) {
				LogSystem.LogDBException(e);
			}	
		}
	}
}