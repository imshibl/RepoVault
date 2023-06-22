package com.bilcodes.repovault.data.remote.api

import com.bilcodes.repovault.data.remote.models.Repository
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GithubApiService {
    @GET("repos/{owner}/{repository}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repository") repository: String
    ): Response<Repository>
}