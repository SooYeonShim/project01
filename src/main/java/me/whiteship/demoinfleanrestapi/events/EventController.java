package me.whiteship.demoinfleanrestapi.events;

import java.net.URI;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@Controller
public class EventController {
	
	
	private final EventRepository eventRepository;
	private final ModelMapper modelMapper;
	private final EventValidator eventValidator;
	
	
	public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
		this.eventRepository=eventRepository;
		this.modelMapper = modelMapper;
		this.eventValidator = eventValidator;
	}
	
	
	@PostMapping("/api/events")
	public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {
		if(errors.hasErrors()) {
			return ResponseEntity.badRequest().body(errors);
		}
		
		eventValidator.validate(eventDto, errors);
		if(errors.hasErrors()) {
			return ResponseEntity.badRequest().build();
		}
		
		Event event = modelMapper.map(eventDto, Event.class);
		Event newEvent = this.eventRepository.save(event);
		
		ControllerLinkBuilder selfLinkbuilder = linkTo(EventController.class).slash(newEvent.getId());
		URI createUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();
		//event.setId(10);
		
		EventResource eventResource = new EventResource(event);
		eventResource.add(linkTo(EventController.class).withRel("query-events"));
		//eventResource.add(selfLinkbuilder.withSelfRel()); //리소스에 넣어줬으므로 삭제
		eventResource.add(selfLinkbuilder.withRel("update-event"));
		return ResponseEntity.created(createUri).body(eventResource);
	}

}	
