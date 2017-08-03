/**
 * Copyright 2016, RadiantBlue Technologies, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.venice.piazza;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.venice.piazza.common.hibernate.dao.DataResourceDao;
import org.venice.piazza.common.hibernate.entity.DataResourceEntity;

import model.data.DataResource;
import model.response.DataResourceListResponse;

@RestController
public class Migration {
	@Autowired
	private DataResourceDao dataResourceDao;

	private static final Logger LOGGER = LoggerFactory.getLogger(Migration.class);

	private RestTemplate restTemplate = new RestTemplate();

	/**
	 * Performs a Migration of Beachfront data from current stores to the new PostgreSQL Store
	 * 
	 * @param environment
	 *            The piazza environment. Int, stage, or prod.
	 * @param doCommit
	 *            True if actual commits should be made. False if read-only mode (For testing)
	 * @return The Status of the migration
	 */
	@RequestMapping(value = "/migrate/{environment}", method = RequestMethod.GET)
	public String migrateEnvironment(@PathVariable(value = "environment") String environment,
			@RequestParam(value = "doCommit", required = false, defaultValue = "false") Boolean doCommit) {
		LOGGER.info(String.format("Beginning migration for %s environment and committing: %s", environment, doCommit.toString()));
		String piazzaHost = environment.equals("prod") ? "geointservices.io" : environment + ".geointservices.io";
		// Get all Data that was ingested by the Task Worker
		List<DataResource> dataList = getDataIngestedByTaskWorker(piazzaHost);
		// Migrate each Data Resource item to Postgres
		migrateDataResource(dataList, doCommit);
		return "Migration complete.";
	}

	public List<DataResource> getDataIngestedByTaskWorker(String piazzaHost) {
		// Get the Data from Access
		String url = String.format("https://pz-access.%s/data?keyword=pzsvc-taskworker&perPage=5000", piazzaHost);
		ResponseEntity<DataResourceListResponse> dataList = restTemplate.getForEntity(url, DataResourceListResponse.class);
		LOGGER.info(String.format("Found %s Data items to Migrate", dataList.getBody().getData().size()));
		return dataList.getBody().getData();
	}

	public void migrateDataResource(List<DataResource> dataList, Boolean doCommit) {
		for (DataResource data : dataList) {
			// Commit to the database
			LOGGER.info(String.format("Committing Data %s to the Database", data.getDataId()));
			if (doCommit.booleanValue()) {
				dataResourceDao.save(new DataResourceEntity(data));
			}
		}
	}

}
