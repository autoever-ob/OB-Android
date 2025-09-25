package com.nobody.campick.services

import com.nobody.campick.models.vehicle.CategoryTypeData
import com.nobody.campick.services.network.APIService
import com.nobody.campick.services.network.ApiResult
import com.nobody.campick.services.network.Endpoint

object CategoryService {

    suspend fun getModelsForType(typeName: String): ApiResult<List<String>> {
        val result = APIService.get<CategoryTypeData>(
            endpoint = Endpoint.CategoryType(typeName)
        )

        return when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.models)
            is ApiResult.Error -> result
        }
    }
}