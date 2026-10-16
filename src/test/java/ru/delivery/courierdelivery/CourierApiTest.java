package ru.delivery.courierdelivery;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.delivery.courierdelivery.repository.RouteRepository;
import ru.delivery.courierdelivery.repository.ShipmentAssignmentRepository;
import ru.delivery.courierdelivery.repository.ShipmentRepository;
import ru.delivery.courierdelivery.repository.StopRepository;
import ru.delivery.courierdelivery.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class CourierApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private ShipmentAssignmentRepository assignmentRepository;

    @BeforeEach
    void clean() {
        assignmentRepository.deleteAll();
        shipmentRepository.deleteAll();
        stopRepository.deleteAll();
        routeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void crudAndWeightLimit() throws Exception {
        long userId = id(mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Иван","email":"Ivan@example.com","role":"SENDER"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andReturn());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Пётр","email":"ivan@example.com","role":"SENDER"}
                                """))
                .andExpect(status().isBadRequest());

        long shipmentId = id(mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senderId":%d,"address":"ул. Ленина, 1","weight":2.5,"status":"CREATED"}
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sender.id").value(userId))
                .andExpect(jsonPath("$.sender.hibernateLazyInitializer").doesNotExist())
                .andReturn());

        mockMvc.perform(get("/api/shipments/" + shipmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("ул. Ленина, 1"))
                .andExpect(jsonPath("$.hibernateLazyInitializer").doesNotExist());

        long heavyId = id(mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senderId":%d,"address":"склад","weight":20,"status":"CREATED"}
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andReturn());

        long routeId = id(mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"routeDate":"2026-10-16","maxWeight":10,"status":"PLANNED"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentWeight").value(0))
                .andReturn());

        long stopId = id(mockMvc.perform(post("/api/stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"routeId":%d,"address":"ул. Ленина, 1","sequenceNumber":1}
                                """.formatted(routeId)))
                .andExpect(status().isCreated())
                .andReturn());

        mockMvc.perform(post("/api/stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"routeId":%d,"address":"другой адрес","sequenceNumber":1}
                                """.formatted(routeId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/shipment-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"shipmentId":%d,"stopId":%d,"status":"PENDING"}
                                """.formatted(heavyId, stopId)))
                .andExpect(status().isBadRequest());

        long assignmentId = id(mockMvc.perform(post("/api/shipment-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"shipmentId":%d,"stopId":%d,"status":"PENDING"}
                                """.formatted(shipmentId, stopId)))
                .andExpect(status().isCreated())
                .andReturn());

        mockMvc.perform(get("/api/routes/" + routeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentWeight").value(2.5))
                .andExpect(jsonPath("$.remainingWeight").value(7.5));

        mockMvc.perform(patch("/api/shipments/" + shipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senderId":%d,"address":"ул. Ленина, 2","weight":3,"status":"ASSIGNED"}
                                """.formatted(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(3))
                .andExpect(jsonPath("$.address").value("ул. Ленина, 2"));

        mockMvc.perform(delete("/api/shipments/" + shipmentId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/shipment-assignments/" + assignmentId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/shipments/" + shipmentId))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/shipments/" + heavyId))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/routes/" + routeId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/stops/" + stopId))
                .andExpect(status().isNotFound());
    }

    private long id(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return node.get("id").asLong();
    }
}
