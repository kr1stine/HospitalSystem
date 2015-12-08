import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;

public class Hospital {
private ArrayList<ExaminationRoom> examinationRooms;
private ArrayList<Doctor> doctors;

	
	public void initializeHospital(){
		// Initialize variables
		
		examinationRooms = new ArrayList<ExaminationRoom>();
		doctors = new ArrayList<Doctor>();
		
		// Add some examination rooms
		ExaminationRoom room100 = new ExaminationRoom(100);
		ExaminationRoom room101 = new ExaminationRoom(101);
		ExaminationRoom room102 = new ExaminationRoom(102);
		ExaminationRoom room103 = new ExaminationRoom(103);
		examinationRooms.add(room100);
		examinationRooms.add(room101);
		examinationRooms.add(room102);
		examinationRooms.add(room103);
		
		// Add some doctors
		doctors.add(new Doctor("Albus", "Dumbeldore", new GregorianCalendar(1860, 3, 12), Specialty.PED, room100));
		doctors.add(new Doctor("Severus", "Snape", new GregorianCalendar(1967, 6, 2), Specialty.GEN, room100));
		doctors.add(new Doctor("Harry", "Potter", new GregorianCalendar(1986, 11, 1), Specialty.CAR, room101));
		// BUG - bad month value
		doctors.add(new Doctor("Hermione", "Granger", new GregorianCalendar(1988, 12, 10), Specialty.PSY, room102));
		
	
	}
	
	public void checkInDoctor(Doctor doctor) throws Exception{
		ArrayList<ExaminationRoom> runningRooms = getRunningRooms();
		if (doctor.isCheckedIn()){
			throw new Exception("Already checked in");
		}
		if (!doctors.contains(doctor)){
			doctors.add(doctor);
		}
		
		// BUG - False positive - possible null-pointer dereference
		ExaminationRoom examinationRoom = null;
		
		if (doctor.getFavoriteRoom().isRunning()){
			boolean foundRoom = false;
			for (int i = 0; i < examinationRooms.size(); i++) {
				if (!examinationRooms.get(i).isRunning()){
					examinationRoom = examinationRooms.get(i);
					foundRoom = true;
					break;
				}
			}
			if (!foundRoom){
				throw new Exception("No rooms available!");
			}
		}
		else if (!doctor.getFavoriteRoom().isRunning()) {
			examinationRoom = doctor.getFavoriteRoom();
		}
		
		// Occupy the examination room
		examinationRoom.setRunning(true);
		examinationRoom.setOccupyingDoctor(doctor);
		
		// Update the doctor's variables
		doctor.setCheckedIn(true);		
		
	}
	
	public void checkOutDoctor(Doctor doctor) throws Exception{
		ArrayList<ExaminationRoom> runningRooms = getRunningRooms();
		if(!doctor.isCheckedIn()){
			throw new Exception("Doctor is not yet checked in!");
		}
		
		// Free examination room and send patients to other rooms
		for (int i = 0; i < runningRooms.size(); i++) {
			ExaminationRoom room = runningRooms.get(i);
			if (room.getOccupyingDoctor() == doctor){	
				if (room.getOccupyingPatient().isCheckedIn()){
					checkOutPatient(room.getOccupyingPatient());
				}
				room.resetExaminationRoom();
				// Find room with shortest waiting list
				
				// TODO: what if empty list?
				
				
				ExaminationRoom minRoom = Collections.min(runningRooms);
			
				// Add people to this list
				// TODO: maybe divide between different rooms
				minRoom.getWaitingPatients().addAll(room.getWaitingPatients());			
				
				break;
			}
		}
		doctor.setCheckedIn(false);
		
	}

	public void checkInPatient(Patient patient) throws Exception{
		// TODO: check if there are available rooms
		ArrayList<ExaminationRoom> runningRooms = getRunningRooms();
		if (patient.isCheckedIn()){
			throw new Exception("Patient already checked in!");
		}
		
		// Find appropriate doctor

		// Lists for rooms with needed specialists and general practitioners
		ArrayList<ExaminationRoom> specialistRooms = new ArrayList<ExaminationRoom>();
		ArrayList<ExaminationRoom> generalRooms = new ArrayList<ExaminationRoom>();
		ArrayList<ExaminationRoom> pediatricianRooms = new ArrayList<ExaminationRoom>();
		
		ExaminationRoom minRoom;
		for (int i = 0; i < runningRooms.size(); i++) {
			ExaminationRoom room = runningRooms.get(i);
			if (patient.getSpecialtyNeeded() == room.getOccupyingDoctor().getSpecialty()){
				specialistRooms.add(room);
			}
			else if (room.getOccupyingDoctor().getSpecialty() == Specialty.GEN){
				generalRooms.add(room);
			}
			else if (room.getOccupyingDoctor().getSpecialty() == Specialty.PED)
				pediatricianRooms.add(room);
		}
		// First, if the patient is younger than 16, he/she is sent to a pediatrician
		if (pediatricianRooms.size() > 0 && patient.age() <= 16){
			minRoom = Collections.min(pediatricianRooms);
		}
		// If specialist was found, add patient to the one with the shortest queue
		else if (specialistRooms.size() > 0){
			minRoom = Collections.min(specialistRooms);
		}
		// Else if a general practitioner was found, add patient to the one with the shortest queue
		else if (generalRooms.size() > 0){
			minRoom = Collections.min(generalRooms);	
		}
		// Else add patient to a random doctor with the shortest queue
		else {
			minRoom = Collections.min(runningRooms);
		}
		minRoom.getWaitingPatients().add(patient);
		patient.setCheckedIn(true);
		
	}
	
	public void checkOutPatient(Patient patient) throws Exception {
		ArrayList<ExaminationRoom> runningRooms = getRunningRooms();
		if (!patient.isCheckedIn()){
			throw new Exception("Patient is not yet checked in!");
		}
		
		// Remove patient from examination room or waiting list
		for (int i = 0; i < runningRooms.size(); i++) {
			ExaminationRoom room = runningRooms.get(i);
			if (room.getOccupyingPatient() == patient){
				room.setOccupyingPatient(null);
				room.setOccupied(false);
			}
			else if (room.getWaitingPatients().contains(patient)){
				room.getWaitingPatients().remove(patient);
			}
		}
		
		patient.setCheckedIn(false);
		
	}
	
	public void updateWaitingLists(){
		ArrayList<ExaminationRoom> runningRooms = getRunningRooms();
		for (int i = 0; i < runningRooms.size(); i++) {
			ExaminationRoom room = runningRooms.get(i);
			// If room is free, try to let a patient enter
			if (room.isRunning() && !room.isOccupied() && room.getWaitingPatients().size() > 0){
				room.setOccupyingPatient(room.getWaitingPatients().remove(0));
			}
		}
	}

	public ArrayList<ExaminationRoom> getRunningRooms(){
		ArrayList<ExaminationRoom> runningRooms = new ArrayList<ExaminationRoom>();
		for (int i = 0; i < examinationRooms.size(); i++) {
			if (examinationRooms.get(i).isRunning()){
				runningRooms.add(examinationRooms.get(i));
			}
		}
		return runningRooms;
	}
	
	public ArrayList<ExaminationRoom> getExaminationRooms() {
		return examinationRooms;
	}

	public void setExaminationRooms(ArrayList<ExaminationRoom> examinationRooms) {
		this.examinationRooms = examinationRooms;
	}

	public ArrayList<Doctor> getDoctors() {
		return doctors;
	}

	public void setDoctors(ArrayList<Doctor> doctors) {
		this.doctors = doctors;
	}
}