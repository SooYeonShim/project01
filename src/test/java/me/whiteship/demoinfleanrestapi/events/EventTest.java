package me.whiteship.demoinfleanrestapi.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import junitparams.Parameters;
import me.whiteship.demoinfleanrestapi.events.Event;

public class EventTest {

	@Test
	public void builder() {
		Event event = Event.builder()
				.name("Inflearn Spring REST API")
				.description("REST API development with Spring")
				.build();
		assertThat(event).isNotNull();
	}
	
	@Test
	public void javaBean(){
		
		// Given
		String name = "Event";
		String description = "Spring";
		
		// When
		Event event = new Event();
		event.setName(name);
		event.setDescription(description);
		
		// Then
		assertThat(event.getName()).isEqualTo(name);
		assertThat(event.getDescription()).isEqualTo(description);
	}
	
	@Test
	@Parameters(method = "paramsForTestFree")
	public void testFree(int basePrice, int maxPrice, boolean isFree) {
		//Given
		Event event = Event.builder()
					.basePrice(0)
					.maxPrice(0)
					.build();
		
		//When
		event.update();
		
		//Then
		assertThat(event.isFree()).isTrue();
		
		
		
		//Given
		event = Event.builder()
				.basePrice(100)
				.maxPrice(0)
				.build();
				
		//When
		event.update();
				
		//Then
		assertThat(event.isFree()).isFalse();
		
		//Given
		event = Event.builder()
				.basePrice(0)
				.maxPrice(100)
				.build();
						
		//When
		event.update();
						
		//Then
		assertThat(event.isFree()).isFalse();		
	}
	
	private Object[] paramsForTestFree() {
		return new Object[] {
				new Object[] {0,0,true},
				new Object[] {100,0,false},
				new Object[] {0,100,false},
				new Object[] {100,200,false}
		};
		
	}
	
	@Test
	@Parameters(method = "parametersForTestOffline")
	public void testOffline(String location, boolean isOffline) {
		//Given
		Event event = Event.builder()
				.location(location)
				.build();
						
		//When
		event.update();
						
		//Then
		assertThat(event.isFree()).isFalse();	
	}
	
	private Object[] parametersForTestOffline() {
		return new Object[] {
				new Object[] {"강남",true},
				new Object[] {null,false},
				new Object[] {"   ",false}
		};
		
	}
	
}
