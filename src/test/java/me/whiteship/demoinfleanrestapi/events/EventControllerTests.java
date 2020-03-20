package me.whiteship.demoinfleanrestapi.events;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import me.whiteship.demoinfleanrestapi.RestDocsConfiguration;



@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test")
public class EventControllerTests {

		@Autowired
		MockMvc mockMvc;
		
		@Autowired
		ObjectMapper objectMapper;
		
		@MockBean
		EventRepository eventRepository;
		
		@Autowired
		ModelMapper modelMapper;
		
		// api.json api.xml		
		@SuppressWarnings("deprecation")
		@Test
		public void createEvent() throws Exception {
			Event event = Event.builder()
						.name("Spring")
						.description("REST API Development with Spring")
						.beginEnrollmentDateTime(LocalDateTime.of(2019, 8,14,16, 0))
						.closeEnrollmentDateTime(LocalDateTime.of(2020, 8,14,16, 0))
						.endEventDateTime(LocalDateTime.of(2019, 8,14,16, 0))
						.basePrice(100)
						.maxPrice(200)
						.limitOfEnrollment(100)
						.location("강남역 D2 스타텁 팩토리")
						.free(true)
						.offline(false)
						.eventStatus(EventStatus.PUBLISHED)
						.build();
			event.setId(10);
			Mockito.when(eventRepository.save(event)).thenReturn(event);
			
			
			mockMvc.perform(post("/api/events/")
					.contentType(MediaType.APPLICATION_JSON_UTF8)
					.accept(MediaTypes.HAL_JSON)
					)
					.andDo(print())
					.andExpect(status().isCreated())
					.andExpect(jsonPath("id").exists())
					.andExpect(header().exists(HttpHeaders.LOCATION))
					.andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/hal+json;charset=UTF-8"))
					.andExpect(jsonPath("id").value(Matchers.not(100))) //아닐 조건
					.andExpect(jsonPath("free").value(Matchers.not(true)))
					.andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
					.andExpect(jsonPath("_links.self").exists())
					.andExpect(jsonPath("_links.query-events").exists())
					.andExpect(jsonPath("_links.update-event").exists())
					.andDo(document("create-event",links(linkWithRel("self").description("link to self"),
							linkWithRel("query").description("link to query events"),
							linkWithRel("update-event").description("link to update an existing event")),
							requestHeaders(headerWithName(HttpHeaders.ACCEPT).description("accept header"),
									headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
								),
							requestFields(
									fieldWithPath("name").description("Name of new event"),
									fieldWithPath("description").description("description of new event"),
									fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
									fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
									fieldWithPath("endEventDateTime").description("date time of end of new event"),
									fieldWithPath("location").description("location of new event"),
									fieldWithPath("basePrice").description("base price of new event"),
									fieldWithPath("maxPrice").description("max price of new event"),
									fieldWithPath("limitOfEnrollment").description("limit of enrollment")
										),
							responseHeaders(
									headerWithName(HttpHeaders.LOCATION).description("Location header"),
									headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
									
									),
							relaxedRequestFields(
									fieldWithPath("id").description("identifier of new event"),
									fieldWithPath("name").description("Name of new event"),
									fieldWithPath("description").description("description of new event"),
									fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
									fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
									fieldWithPath("endEventDateTime").description("date time of end of new event"),
									fieldWithPath("location").description("location of new event"),
									fieldWithPath("basePrice").description("base price of new event"),
									fieldWithPath("maxPrice").description("max price of new event"),
									fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
									fieldWithPath("free").description("it tells is this event is free or not"),
									fieldWithPath("offline").description("it tells is this event is offline or not"),
									fieldWithPath("eventStatus").description("event status")
										)
							
							
							))
				
					;
		}
		
		@Test
		public void createEvent_Bad_Request_Empty_Input() throws Exception{
		EventDto eventDto = EventDto.builder().build();
		 this.mockMvc.perform(post("/api/events")
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.content(this.objectMapper.writeValueAsString(eventDto)))
			.andExpect(status().isBadRequest());
		}
		
		
		@Test
		public void queryEvents() throws Exception{
			//Given
			IntStream.range(0, 30).forEach(this::generateEvent);
			
			//When
			this.mockMvc.perform(get("/api/events")
					.param("page", "1")
					.param("size","1")
					.param("sort","name,DESC")
					)
						.andDo(print())
						.andExpect(status().isOk())
						.andExpect(jsonPath("page").exists())
						.andExpect(jsonPath("_embedded.eventList[0].self").exists())
						.andExpect(jsonPath("_links.self").exists())
						.andExpect(jsonPath("_links.profile").exists())
						.andDo(document("query-events"))
						;
			
		
		}
		
		@Test
		public void getEvent() throws Exception{
			//Given
			Event event = this.generateEvent(100);
			
			//When & Then
			this.mockMvc.perform(get("/api/evenets/{id}",event.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("id").exists())
				.andExpect(jsonPath("name").exists())
				.andExpect(jsonPath("_links.self").exists())
				.andExpect(jsonPath("_links.profile").exists())
				.andDo(document("query-events"))
			;
		}

		@Test
		public void getEvent404() throws Exception{
			this.mockMvc.perform(get("/api/events/11883"))
			.andExpect(status().isNotFound())
			
			;
			
		}
		
		@Test
		public void updateEvent() throws Exception {
		
		Event event = this.generateEvent(200);
		EventDto eventDto = this.modelMapper.map(event, EventDto.class);
		String eventName = "Updated Event";
		
		eventDto.setName(eventName);
		
		this.mockMvc.perform(get("/api/events/{id}",event.getId())
			
				)
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("name").value(eventName))
		.andExpect(jsonPath("_link.self").exists())
		
		
		;
			
		}
		
		@Test
		public void updateEvent400() throws Exception {
		
		Event event = this.generateEvent(200);
		EventDto eventDto = new EventDto();
		String eventName = "Updated Event";
		
		eventDto.setName(eventName);
		
		this.mockMvc.perform(get("/api/events/{id}",event.getId()))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("name").value(eventName))
		.andExpect(jsonPath("_link.self").exists())
		
		
		;
			
		}
		
		
		private Event generateEvent(int index) {
			
			Event event = Event.builder()
					.name("event "+index)
					.description("test event")
					.build();
			
			
			return this.eventRepository.save(event);
		}
}
