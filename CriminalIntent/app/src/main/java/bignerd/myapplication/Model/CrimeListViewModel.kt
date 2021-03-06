package bignerd.myapplication.Model

import androidx.lifecycle.ViewModel
import bignerd.myapplication.CrimeRepository
import kotlin.random.Random

class CrimeListViewModel:ViewModel() {


   /*val crimes = mutableListOf<Crime>()

    init {
        for (i in 0 until 100) {
            val crime = Crime()
            crime.title = "Crime #$i"
            crime.isSolved = i % 2 == 0
            crime.requiresPolice = i % 5 == 0
            crimes += crime
        }
    }*/

    private val crimeRepository = CrimeRepository.get()
//    val crimes = crimeRepository.getCrimes()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime) = crimeRepository.addCrime(crime)
}