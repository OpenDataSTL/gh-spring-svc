package svc.managers;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;

import svc.data.municipal.MunicipalityDAO;
import svc.models.Municipality;

@RunWith(MockitoJUnitRunner.class)
public class MunicipalityManagerTest {

	@InjectMocks
	MunicipalityManager manager;
	
	@Mock
	MunicipalityDAO municipalityDAOMock;
	
	@Test
	public void returnsMunicipalitiesFromCourtId(){
		final Municipality MUNICIPALITY = new Municipality();
		final long COURTID = 1L;
		final List<Municipality> MUNICIPALITIES = Arrays.asList(new Municipality[]{MUNICIPALITY});
		when(municipalityDAOMock.getByCourtId(COURTID)).thenReturn(MUNICIPALITIES);
		assertThat(manager.GetMunicipalitiesByCourtId(COURTID),equalTo(MUNICIPALITIES));
	}
	
	@Test
	public void returnsMunicipalityFromMunicipalityId(){
		final Municipality MUNICIPALITY = new Municipality();
		final long MUNICIPALITYID = 1L;
		when(municipalityDAOMock.getByMunicipalityId(MUNICIPALITYID)).thenReturn(MUNICIPALITY);
		assertThat(manager.GetMunicipalityById(MUNICIPALITYID),equalTo(MUNICIPALITY));
	}
	
	@Test
	public void returnsAllMunicipalities(){
		final List<Municipality> MUNICIPALITIES = Arrays.asList(new Municipality[]{new Municipality()});
		when(municipalityDAOMock.getAllMunicipalities(null)).thenReturn(MUNICIPALITIES);
		assertThat(manager.GetAllMunicipalities(null),equalTo(MUNICIPALITIES));
	}
	
}
