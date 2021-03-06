package svc.data.citations.datasources.tyler.transformers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import svc.data.citations.datasources.CITATION_DATASOURCE;
import svc.data.citations.datasources.transformers.CourtIdTransformer;
import svc.data.citations.datasources.transformers.MunicipalityIdTransformer;
import svc.data.citations.datasources.tyler.models.TylerCitation;
import svc.data.citations.datasources.tyler.models.TylerViolation;
import svc.data.transformer.CitationDateTimeTransformer;
import svc.models.Citation;
import svc.models.Municipality;
import svc.types.HashableEntity;

@RunWith(MockitoJUnitRunner.class)
public class TylerCitationTransformerTest {

	@Mock
	TylerViolationTransformer violationTransformer;

	@Mock
	CourtIdTransformer courtIdTransformer;

	@InjectMocks
	TylerCitationTransformer citationTransformer;

	@Mock
	MunicipalityIdTransformer municipalityIdTransformer;
	
	@Mock
	CitationDateTimeTransformer citationDateTimeTransformer;
	
	@Test
	public void citationTransformerReturnsNullForEmptyLists() {

		List<Citation> genericCitations = citationTransformer.fromTylerCitations(null);

		assertNull(genericCitations);
	}

	private List<TylerCitation> generateListOfTylerCitations() {
		return generateListOfTylerCitations(true);
	}

	private List<TylerCitation> generateListOfTylerCitations(boolean withCitations) {
		List<TylerCitation> listOfCitations = Lists.newArrayList(mock(TylerCitation.class));

		if (withCitations) {
			for (TylerCitation citation : listOfCitations) {
				citation.violations = generateListOfTylerViolations();
			}
		}

		return listOfCitations;
	}

	private List<TylerViolation> generateListOfTylerViolations() {
		return Lists.newArrayList(mock(TylerViolation.class));
	}

	@Test
	public void citationTransformerTransformsAllCitationsPassedIn() {
		final HashableEntity<Municipality> municipalHashable = new HashableEntity<Municipality>(Municipality.class,3L);
		when(municipalityIdTransformer.lookupMunicipalityId(anyObject(),anyString()))
		.thenReturn(municipalHashable);
		
		List<TylerCitation> tylerCitations = generateListOfTylerCitations();

		List<Citation> genericCitations = citationTransformer.fromTylerCitations(tylerCitations);

		assertNotNull(genericCitations);
		assertEquals(tylerCitations.size(), genericCitations.size());
	}

	@Test
	public void citationTransformerReturnsNullForNullCitation() {

		Citation genericCitation = citationTransformer.fromTylerCitation(null);

		assertNull(genericCitation);
	}

	private TylerCitation generateFullTylerCitation() {
		TylerCitation mockCitation = mock(TylerCitation.class);
		mockCitation.dob = "06/17/1900";
		mockCitation.violationDate = "06/17/1901";

		mockCitation.violations = generateListOfTylerViolations();
		mockCitation.violations.get(0).courtDate = "1902-06-17T19:00:00.000";
		mockCitation.violations.get(0).courtName = "A";

		return mockCitation;
	}

	@Test
	public void citationTransformerCopiesCitationFieldsCorrectly() {
		TylerCitation tylerCitation = generateFullTylerCitation();
		final HashableEntity<Municipality> municipalHashable = new HashableEntity<Municipality>(Municipality.class,3L);
		when(municipalityIdTransformer.lookupMunicipalityId(anyObject(),anyString()))
		.thenReturn(municipalHashable);
		
		ZonedDateTime zonedCourtDateTime = ZonedDateTime.of(LocalDateTime.parse("1902-06-17T19:00:00.000"),ZoneId.of("America/Chicago"));
		when(citationDateTimeTransformer.transformLocalDateTime(any(), any())).thenReturn(zonedCourtDateTime);
		
		Citation genericCitation = citationTransformer.fromTylerCitation(tylerCitation);

		assertNotNull(genericCitation);
		assertEquals(genericCitation.citation_number, tylerCitation.citationNumber);
		assertEquals(genericCitation.first_name, tylerCitation.firstName);
		assertEquals(genericCitation.last_name, tylerCitation.lastName);
		assertEquals(genericCitation.drivers_license_number, tylerCitation.driversLicenseNumber);
		assertEquals(genericCitation.date_of_birth, LocalDate.parse("1900-06-17"));
		assertEquals(genericCitation.citation_date, LocalDate.parse("1901-06-17"));
		assertEquals(genericCitation.court_dateTime, zonedCourtDateTime);
		assertEquals(genericCitation.municipality_id, municipalHashable);

		verify(violationTransformer).fromTylerCitation(tylerCitation);
		verify(courtIdTransformer).lookupCourtId(CITATION_DATASOURCE.TYLER,"A");
	}
}
