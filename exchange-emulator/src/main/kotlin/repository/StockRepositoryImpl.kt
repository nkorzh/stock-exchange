package repository

import model.Company

class StockRepositoryImpl : StockRepository {
    private val nameToCompany: MutableMap<String, Company> = mutableMapOf()

    override fun addCompany(company: Company) {
        nameToCompany[company.name] = company
    }

    override fun getCompanyByName(name: String): Company? = nameToCompany[name]

    override fun getAllCompanies(): List<Company> = nameToCompany.values.toList()

    override fun updateCompany(company: Company) {
        nameToCompany[company.name] = company
    }

    override fun clear() = nameToCompany.clear()
}
