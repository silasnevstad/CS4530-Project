// Owner: Silas Nevstad

package com.group12.husksheets.ui;

import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Result;
import com.group12.husksheets.ui.services.BackendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackendServiceTest {

    private BackendService backendService;
    private BackendService backendServiceSpy;

    @BeforeEach
    void setUp() {
        backendService = new BackendService("username", "password");
        backendServiceSpy = Mockito.spy(backendService);
    }

    @Test
    void testRegister_Success() throws Exception {
        Result registerResult = new Result(true, "Publisher registered", null);
        doReturn(registerResult).when(backendServiceSpy).register(anyString());

        Result result = backendServiceSpy.register("newPublisher");

        assertTrue(result.success);
        assertEquals("Publisher registered", result.message);
    }

    @Test
    void testRegister_Failure() throws Exception {
        Result registerResult = new Result(false, "Publisher already exists", null);
        doReturn(registerResult).when(backendServiceSpy).register(anyString());

        Result result = backendServiceSpy.register("existingPublisher");

        assertFalse(result.success);
        assertEquals("Publisher already exists", result.message);
    }

    @Test
    void testDoesPublisherExist_Exists() throws Exception {
        Result publishersResult = new Result(true, "Success", Collections.singletonList(new Argument("existingPublisher", null, null, null)));
        doReturn(publishersResult).when(backendServiceSpy).getPublishers();

        boolean exists = backendServiceSpy.doesPublisherExist("existingPublisher");

        assertTrue(exists);
    }

    @Test
    void testDoesPublisherExist_DoesNotExist() throws Exception {
        Result publishersResult = new Result(true, "Success", Collections.emptyList());
        doReturn(publishersResult).when(backendServiceSpy).getPublishers();

        boolean exists = backendServiceSpy.doesPublisherExist("nonExistentPublisher");

        assertFalse(exists);
    }

    @Test
    void testCreateSheet_Success() throws Exception {
        Result createSheetResult = new Result(true, "Sheet created", null);
        doReturn(createSheetResult).when(backendServiceSpy).createSheet(anyString(), anyString());

        Result result = backendServiceSpy.createSheet("publisher", "newSheet");

        assertTrue(result.success);
        assertEquals("Sheet created", result.message);
    }

    @Test
    void testCreateSheet_Failure() throws Exception {
        Result createSheetResult = new Result(false, "Sheet already exists", null);
        doReturn(createSheetResult).when(backendServiceSpy).createSheet(anyString(), anyString());

        Result result = backendServiceSpy.createSheet("publisher", "existingSheet");

        assertFalse(result.success);
        assertEquals("Sheet already exists", result.message);
    }

    @Test
    void testDeleteSheet_Success() throws Exception {
        Result deleteSheetResult = new Result(true, "Sheet deleted", null);
        doReturn(deleteSheetResult).when(backendServiceSpy).deleteSheet(anyString(), anyString());

        Result result = backendServiceSpy.deleteSheet("publisher", "existingSheet");

        assertTrue(result.success);
        assertEquals("Sheet deleted", result.message);
    }

    @Test
    void testDeleteSheet_Failure() throws Exception {
        Result deleteSheetResult = new Result(false, "Sheet does not exist", null);
        doReturn(deleteSheetResult).when(backendServiceSpy).deleteSheet(anyString(), anyString());

        Result result = backendServiceSpy.deleteSheet("publisher", "nonExistentSheet");

        assertFalse(result.success);
        assertEquals("Sheet does not exist", result.message);
    }

    @Test
    void testGetSheets_Success() throws Exception {
        Result getSheetsResult = new Result(true, "Success", Collections.singletonList(new Argument("publisher", "sheet1", null, null)));
        doReturn(getSheetsResult).when(backendServiceSpy).getSheets(anyString());

        Result result = backendServiceSpy.getSheets("publisher");

        assertTrue(result.success);
        assertEquals("Success", result.message);
        assertFalse(result.value.isEmpty());
    }

    @Test
    void testGetSheets_NoSheets() throws Exception {
        Result getSheetsResult = new Result(true, "Success", Collections.emptyList());
        doReturn(getSheetsResult).when(backendServiceSpy).getSheets(anyString());

        Result result = backendServiceSpy.getSheets("publisher");

        assertTrue(result.success);
        assertEquals("Success", result.message);
        assertTrue(result.value.isEmpty());
    }

    @Test
    void testUpdateSheet_Success() throws Exception {
        Result updateSheetResult = new Result(true, "Update published", null);
        doReturn(updateSheetResult).when(backendServiceSpy).updateSheet(anyString(), anyString(), anyString(), anyBoolean());

        Result result = backendServiceSpy.updateSheet("publisher", "sheet", "newData", true);

        assertTrue(result.success);
        assertEquals("Update published", result.message);
    }

    @Test
    void testUpdateSheet_Failure() throws Exception {
        Result updateSheetResult = new Result(false, "Publisher or sheet does not exist", null);
        doReturn(updateSheetResult).when(backendServiceSpy).updateSheet(anyString(), anyString(), anyString(), anyBoolean());

        Result result = backendServiceSpy.updateSheet("publisher", "nonExistentSheet", "newData", true);

        assertFalse(result.success);
        assertEquals("Publisher or sheet does not exist", result.message);
    }

    @Test
    void testGetPublishers_Success() throws Exception {
        Result getPublishersResult = new Result(true, "Success", Collections.singletonList(new Argument("publisher", null, null, null)));
        doReturn(getPublishersResult).when(backendServiceSpy).getPublishers();

        Result result = backendServiceSpy.getPublishers();

        assertTrue(result.success);
        assertEquals("Success", result.message);
        assertFalse(result.value.isEmpty());
    }

    @Test
    void testGetPublishers_NoPublishers() throws Exception {
        Result getPublishersResult = new Result(true, "Success", Collections.emptyList());
        doReturn(getPublishersResult).when(backendServiceSpy).getPublishers();

        Result result = backendServiceSpy.getPublishers();

        assertTrue(result.success);
        assertEquals("Success", result.message);
        assertTrue(result.value.isEmpty());
    }
}