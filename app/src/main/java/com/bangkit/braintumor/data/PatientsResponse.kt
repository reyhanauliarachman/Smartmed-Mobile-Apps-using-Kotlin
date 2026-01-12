//PatientsResponse
package com.bangkit.braintumor.data

import com.google.gson.annotations.SerializedName

data class PatientsResponse(

	@field:SerializedName("patient")
	val patient: Patient? = null
)

data class Patient(

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("komplikasi")
	val komplikasi: String? = null,

	@field:SerializedName("age")
	val age: Int? = null,

	@field:SerializedName("email")
	val email: String? = null
)
