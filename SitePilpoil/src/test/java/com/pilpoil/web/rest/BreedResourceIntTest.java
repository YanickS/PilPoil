package com.pilpoil.web.rest;

import com.pilpoil.Application;
import com.pilpoil.domain.Breed;
import com.pilpoil.repository.BreedRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the BreedResource REST controller.
 *
 * @see BreedResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class BreedResourceIntTest {

    private static final String DEFAULT_LABEL = "AAAAA";
    private static final String UPDATED_LABEL = "BBBBB";

    @Inject
    private BreedRepository breedRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restBreedMockMvc;

    private Breed breed;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        BreedResource breedResource = new BreedResource();
        ReflectionTestUtils.setField(breedResource, "breedRepository", breedRepository);
        this.restBreedMockMvc = MockMvcBuilders.standaloneSetup(breedResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        breed = new Breed();
        breed.setLabel(DEFAULT_LABEL);
    }

    @Test
    @Transactional
    public void createBreed() throws Exception {
        int databaseSizeBeforeCreate = breedRepository.findAll().size();

        // Create the Breed

        restBreedMockMvc.perform(post("/api/breeds")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(breed)))
                .andExpect(status().isCreated());

        // Validate the Breed in the database
        List<Breed> breeds = breedRepository.findAll();
        assertThat(breeds).hasSize(databaseSizeBeforeCreate + 1);
        Breed testBreed = breeds.get(breeds.size() - 1);
        assertThat(testBreed.getLabel()).isEqualTo(DEFAULT_LABEL);
    }

    @Test
    @Transactional
    public void getAllBreeds() throws Exception {
        // Initialize the database
        breedRepository.saveAndFlush(breed);

        // Get all the breeds
        restBreedMockMvc.perform(get("/api/breeds?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(breed.getId().intValue())))
                .andExpect(jsonPath("$.[*].label").value(hasItem(DEFAULT_LABEL.toString())));
    }

    @Test
    @Transactional
    public void getBreed() throws Exception {
        // Initialize the database
        breedRepository.saveAndFlush(breed);

        // Get the breed
        restBreedMockMvc.perform(get("/api/breeds/{id}", breed.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(breed.getId().intValue()))
            .andExpect(jsonPath("$.label").value(DEFAULT_LABEL.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingBreed() throws Exception {
        // Get the breed
        restBreedMockMvc.perform(get("/api/breeds/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBreed() throws Exception {
        // Initialize the database
        breedRepository.saveAndFlush(breed);

		int databaseSizeBeforeUpdate = breedRepository.findAll().size();

        // Update the breed
        breed.setLabel(UPDATED_LABEL);

        restBreedMockMvc.perform(put("/api/breeds")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(breed)))
                .andExpect(status().isOk());

        // Validate the Breed in the database
        List<Breed> breeds = breedRepository.findAll();
        assertThat(breeds).hasSize(databaseSizeBeforeUpdate);
        Breed testBreed = breeds.get(breeds.size() - 1);
        assertThat(testBreed.getLabel()).isEqualTo(UPDATED_LABEL);
    }

    @Test
    @Transactional
    public void deleteBreed() throws Exception {
        // Initialize the database
        breedRepository.saveAndFlush(breed);

		int databaseSizeBeforeDelete = breedRepository.findAll().size();

        // Get the breed
        restBreedMockMvc.perform(delete("/api/breeds/{id}", breed.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Breed> breeds = breedRepository.findAll();
        assertThat(breeds).hasSize(databaseSizeBeforeDelete - 1);
    }
}
