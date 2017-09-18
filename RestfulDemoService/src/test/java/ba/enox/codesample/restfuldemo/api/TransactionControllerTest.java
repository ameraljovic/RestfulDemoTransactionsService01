package ba.enox.codesample.restfuldemo.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import ba.enox.codesample.restfuldemo.db.TransactionsHome;
import ba.enox.codesample.restfuldemo.model.Transaction;
import ba.enox.codesample.restfuldemo.model.TransactionStatistic;
import ba.enox.codesample.restfuldemo.service.TransactionService;
import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;




@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

	
	/*
	 * Used to simulate full end to end case.
	 */
	@MockBean
	TransactionsHome transactionsHome;

	@Autowired
    private TransactionController controller;
	
	
    @Autowired
    private MockMvc mockMvc;
    
    @Before
    public void beforeTests(){
    	assertTrue("Controller need to be initialized ",controller!=null);
    	MockitoAnnotations.initMocks(this);
    	
    	ConcurrentNavigableMap<Long, List<Transaction>> cnm = new ConcurrentSkipListMap<Long, List<Transaction>>();
    	
    	
    	List<Transaction> listOneMinnuteAfter = new ArrayList<>();
    	Long oneMinAfter = ((Instant.now().getEpochSecond()+60)*1000);//*1000 to add three time 0 as in sample
    	listOneMinnuteAfter.add(new Transaction(10.0D, oneMinAfter));
    	listOneMinnuteAfter.add(new Transaction(20.0D, oneMinAfter));
		cnm.put(oneMinAfter, listOneMinnuteAfter);
			
    	System.out.println("one min after "+oneMinAfter);
    	Mockito.when(transactionsHome.getTransactions()).thenReturn(cnm);
    }
    
    
    
    @Test
	public void transactionsMockedDbLayerIsNoContentCaseTest() throws Exception {
    	Transaction t = new Transaction(157.00D, (Instant.now().getEpochSecond()-70)*1000);
       

        mockMvc.perform(post("/transactions")
        		.content(asJsonString(t))
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
    
    @Test
    public void transactionsMockedDbLayerSuccesfulCaseTest() throws Exception {
    	Transaction t = new Transaction(157.00D, (Instant.now().getEpochSecond()+60)*1000);
       

        mockMvc.perform(post("/transactions")
        		.content(asJsonString(t))
                .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
    
    
    @Test
    public void getStatisticsMockedDBLayerTest() throws Exception {
    	
    	//transactionSevice.getStatisticInTime(System.currentTimeMillis()-12000);
    	
    	
        TransactionStatistic ts = new TransactionStatistic();
        ts.setAvg(15D);
        ts.setCount(2l);
        ts.setMax(20D);
        ts.setMin(10D);
        ts.setSum(30D);
       // given(controller.last60SecsStatistic()).willReturn(new ResponseEntity<TransactionStatistic>(ts,HttpStatus.OK));

        mockMvc.perform(get("/statistics")
          
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
               // .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("avg", is(ts.getAvg())))
                .andExpect(jsonPath("count", is(Integer.valueOf(ts.getCount()+""))))//some issue with long
                .andExpect(jsonPath("min", is(ts.getMin())))
                .andExpect(jsonPath("max", is(ts.getMax())))
                .andExpect(jsonPath("sum", is(ts.getSum()))
                );
    }
    

    /*
     * Helper json method
     */
    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }  
    

}