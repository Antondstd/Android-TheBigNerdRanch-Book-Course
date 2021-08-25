package bignerd.myapplication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings.System.DATE_FORMAT
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import bignerd.myapplication.Model.Crime
import java.io.File
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = "DialogDate"
private const val REQUEST_CONTACT = 1

class CrimeFragment : Fragment(), FragmentResultListener {
    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button

    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        Log.d(TAG, "args bundle crime ID: $crimeId")
        crimeDetailViewModel.loadCrime(crimeId)
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply { putSerializable(ARG_CRIME_ID, crimeId) }
            return CrimeFragment().apply { arguments = args }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(viewLifecycleOwner, Observer { crime ->
            crime?.let {
                this.crime = crime
                photoFile = crimeDetailViewModel.getPhotoFile(crime)
                photoUri = FileProvider.getUriForFile(
                    requireActivity(),
                    "bignerd.myapplication.criminalintent.fileprovider",
                    photoFile
                )
                updateUI()
            }
        })
        childFragmentManager.setFragmentResultListener(REQUEST_DATE, viewLifecycleOwner, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView

        dateButton.apply {
            text = crime.date.toString()
            isEnabled = true
        }

        return view
    }

    val resultContact =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when {
                result.resultCode != Activity.RESULT_OK -> return@registerForActivityResult
                result.data != null -> {
                    val contactUri: Uri? = result.data!!.data
                    val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                    val cursor = contactUri?.let {
                        requireActivity().contentResolver.query(
                            it,
                            queryFields,
                            null,
                            null,
                            null
                        )
                    }
                    cursor?.use {
                        if (it.count == 0) return@registerForActivityResult
                        it.moveToFirst()
                        val suspect = it.getString(0)
                        crime.suspect = suspect
                        crimeDetailViewModel.saveCrime(crime)
                        suspectButton.text = suspect
                    }
                }
            }

        }

    val resultImageCapture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            updatePhotoView()
        }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
// This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
// This one too
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            Log.d(TAG, "Clicking on Date button")
            DataPickerFragment.newInstance(crime.date, REQUEST_DATE).apply {
//                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(
                    this@CrimeFragment.childFragmentManager,
                    DIALOG_DATE
                )
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                startActivity(
                    Intent.createChooser(
                        intent,
                        getString(R.string.send_report)
                    )
                )
            }
        }

        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener { resultContact.launch(pickContactIntent) }
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null)
                isEnabled = false
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                Log.d(TAG, "No photoapps for this one")
                isEnabled = false
            }

            setOnClickListener {
                Log.d(TAG, "Photobutton clicked")
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(
                    captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                resultImageCapture.launch(captureImage)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onFragmentResult(requestCode: String, result: Bundle) {
        when (requestCode) {
            REQUEST_DATE -> {
                Log.d(TAG, "received result for $requestCode")
                crime.date = DataPickerFragment.getSelectedDate(result)
                updateUI()
            }
        }
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty())
            suspectButton.text = crime.suspect
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else photoView.setImageDrawable(null)
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved)
            getString(R.string.crime_report_solved)
        else
            getString(R.string.crime_report_unsolved)
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank())
            getString(R.string.crime_report_no_suspect)
        else
            getString(R.string.crime_report_suspect, crime.suspect)

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    class CrimeDetailViewModel() : ViewModel() {
        private val crimeRepository = CrimeRepository.get()
        private val crimeIdLiveData = MutableLiveData<UUID>()

        var crimeLiveData: LiveData<Crime?> =
            Transformations.switchMap(crimeIdLiveData) { crimeId ->
                crimeRepository.getCrime(crimeId)
            }

        fun loadCrime(crimeId: UUID) {
            crimeIdLiveData.value = crimeId
        }

        fun saveCrime(crime: Crime) {
            crimeRepository.updateCrime(crime)
        }

        fun getPhotoFile(crime: Crime): File = crimeRepository.getPhotoFile(crime)
    }

}