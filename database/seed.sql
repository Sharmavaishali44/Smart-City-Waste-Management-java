-- =======================================================
-- Smart City Waste Collection & Municipal Complaint System
-- Official Seed Data (Nagar Nigam Jaipur)
-- =======================================================

USE smart_city_waste;

-- -------------------------------------------------------
-- 1. SEED USERS
-- -------------------------------------------------------
-- Admin: 'Admin@2026' (hash: 8205fe709a2ba516367358130ba86fbcd10eb440a6eb3433b2314e29dea9a91f, salt: salt_admin)
-- Citizens: 'password123' (hash: 823fdf91c957a21ff86a3f9961f097458f60907b75487cfb48279366a63e9312, salt: salt_citizen)
-- Crews: 'crew123' (hash: ea5b66b5685644cbc3c47c50c7cfbc620fe016b34b9a81f8a95c7efd3830fffe, salt: salt_crew)

INSERT INTO users (id, username, password_hash, salt, full_name, email, phone, role, is_active) VALUES
-- Authorized Demo Municipal Administrator
(1, 'admin', '8205fe709a2ba516367358130ba86fbcd10eb440a6eb3433b2314e29dea9a91f', 'salt_admin', 'Shri Rajeshwar Sharma, IAS', 'admin@smartcityjaipur.gov.in', '9829012345', 'ADMIN', TRUE),

-- Crew Accounts (Managed strictly by Admin)
(2, 'crew_mansarovar', 'ea5b66b5685644cbc3c47c50c7cfbc620fe016b34b9a81f8a95c7efd3830fffe', 'salt_crew', 'Ramesh Sharma', 'crew.mansarovar@smartcityjaipur.gov.in', '9828011111', 'CREW', TRUE),
(3, 'crew_malviya', 'ea5b66b5685644cbc3c47c50c7cfbc620fe016b34b9a81f8a95c7efd3830fffe', 'salt_crew', 'Dinesh Meena', 'crew.malviya@smartcityjaipur.gov.in', '9828022222', 'CREW', TRUE),
(4, 'crew_sanganer', 'ea5b66b5685644cbc3c47c50c7cfbc620fe016b34b9a81f8a95c7efd3830fffe', 'salt_crew', 'Suresh Yadav', 'crew.sanganer@smartcityjaipur.gov.in', '9828033333', 'CREW', TRUE),
(5, 'crew_civillines', 'ea5b66b5685644cbc3c47c50c7cfbc620fe016b34b9a81f8a95c7efd3830fffe', 'salt_crew', 'Manoj Verma', 'crew.civillines@smartcityjaipur.gov.in', '9828044444', 'CREW', TRUE),
(6, 'crew_vidyadhar', 'ea5b66b5685644cbc3c47c50c7cfbc620fe016b34b9a81f8a95c7efd3830fffe', 'salt_crew', 'Mukesh Gurjar', 'crew.vidyadhar@smartcityjaipur.gov.in', '9828055555', 'CREW', TRUE),

-- Citizens
(7, 'rahul', '823fdf91c957a21ff86a3f9961f097458f60907b75487cfb48279366a63e9312', 'salt_citizen', 'Rahul Verma', 'rahul@example.com', '9876543210', 'CITIZEN', TRUE),
(8, 'priya', '823fdf91c957a21ff86a3f9961f097458f60907b75487cfb48279366a63e9312', 'salt_citizen', 'Priya Sharma', 'priya.s@example.com', '9876543211', 'CITIZEN', TRUE),
(9, 'amit', '823fdf91c957a21ff86a3f9961f097458f60907b75487cfb48279366a63e9312', 'salt_citizen', 'Amit Patel', 'amit.patel@example.com', '9876543212', 'CITIZEN', TRUE),
(10, 'sneha', '823fdf91c957a21ff86a3f9961f097458f60907b75487cfb48279366a63e9312', 'salt_citizen', 'Sneha Gupta', 'sneha.g@example.com', '9876543213', 'CITIZEN', TRUE),
(11, 'vikram', '823fdf91c957a21ff86a3f9961f097458f60907b75487cfb48279366a63e9312', 'salt_citizen', 'Vikram Singh', 'vikram.s@example.com', '9876543214', 'CITIZEN', TRUE);

-- -------------------------------------------------------
-- 2. SEED OFFICIAL JAIPUR AREAS (ZONES)
-- -------------------------------------------------------
INSERT INTO areas (id, name, city, population, is_sensitive) VALUES
(1, 'Mansarovar Zone', 'Jaipur', 145000, FALSE),
(2, 'Malviya Nagar Zone', 'Jaipur', 120000, FALSE),
(3, 'Sanganer Zone', 'Jaipur', 160000, FALSE),
(4, 'Civil Lines Zone', 'Jaipur', 75000, TRUE),   -- VIP / Government Administrative Area
(5, 'Vidyadhar Nagar Zone', 'Jaipur', 135000, FALSE),
(6, 'Moti Dungri / Raja Park Zone', 'Jaipur', 90000, FALSE),
(7, 'Hawa Mahal / Amer Zone', 'Jaipur', 110000, TRUE), -- Heritage / Tourism High Density
(8, 'Kishanpole Zone', 'Jaipur', 85000, TRUE);         -- Walled City Commercial Corridor

-- -------------------------------------------------------
-- 3. SEED OFFICIAL JAIPUR WARDS
-- -------------------------------------------------------
INSERT INTO wards (id, ward_number, ward_name, area_id) VALUES
-- Mansarovar Zone (Wards 43 - 47)
(1, 'Ward 43', 'Sector 1 to 5 (Shipra Path & Technology Park)', 1),
(2, 'Ward 44', 'Sector 6 to 9 (Madhyam Marg & Metro Corridor)', 1),
(3, 'Ward 45', 'Sector 10 to 12 (Kiran Path & Swarn Path)', 1),
(4, 'Ward 46', 'Sector 13 to 14 (Varun Path & VT Road)', 1),
(5, 'Ward 47', 'Kaveri Path & Ganga Marg Residential Colony', 1),

-- Malviya Nagar Zone (Wards 51 - 55)
(6, 'Ward 51', 'Calgiri Hospital Marg & Sector 1', 2),
(7, 'Ward 52', 'Central Market & Sector 3 Plaza', 2),
(8, 'Ward 53', 'Girdhar Marg & Sector 5', 2),
(9, 'Ward 54', 'Model Town & Jagatpura Phatak Road', 2),
(10, 'Ward 55', 'Malviya Industrial Area & Apex Circle', 2),

-- Sanganer Zone (Wards 61 - 65)
(11, 'Ward 61', 'Sanganer Old Town & Paper Colony', 3),
(12, 'Ward 62', 'Pratap Nagar Sector 8 & Kumbha Marg', 3),
(13, 'Ward 63', 'Pratap Nagar Haldi Ghati Marg', 3),
(14, 'Ward 64', 'Sitapura Industrial Area Phase 1-2', 3),
(15, 'Ward 65', 'Muhana Mandi & RIICO Gate', 3),

-- Civil Lines Zone (Wards 15 - 19)
(16, 'Ward 15', 'Raj Bhavan & High Court VIP Colony', 4),
(17, 'Ward 16', 'Secretariat & Government Press Area', 4),
(18, 'Ward 17', 'Hasanpura & Railway Transit Colony', 4),
(19, 'Ward 18', 'Sodala Sabzi Mandi & Ajmer Road', 4),
(20, 'Ward 19', 'Jacob Road & Civil Lines Metro Hub', 4),

-- Vidyadhar Nagar Zone (Wards 1 - 5)
(21, 'Ward 1', 'Central Spine & Sector 1 Market', 5),
(22, 'Ward 2', 'Sector 2 D-Block & National Handloom Circle', 5),
(23, 'Ward 3', 'Ambabari Colony & Sikar Road Overbridge', 5),
(24, 'Ward 4', 'Murlipura Scheme & Dadi Ka Phatak', 5),
(25, 'Ward 5', 'VKI Road No. 1 to 5 Industrial Beat', 5),

-- Moti Dungri / Raja Park Zone (Wards 31 - 34)
(26, 'Ward 31', 'Raja Park Main Market & Gali 1-10', 6),
(27, 'Ward 32', 'Panchwati Colony & Gurudwara Lane', 6),
(28, 'Ward 33', 'Adarsh Nagar & Frontier Colony', 6),
(29, 'Ward 34', 'Tilak Nagar & Sethi Colony Park', 6),

-- Hawa Mahal / Amer Zone (Wards 71 - 74)
(30, 'Ward 71', 'Badi Chaupar & Johari Bazaar Heritage Walk', 7),
(31, 'Ward 72', 'Subhash Chowk & Ramganj Bazaar', 7),
(32, 'Ward 73', 'Jal Mahal Tourist Promanade & Kanak Ghati', 7),
(33, 'Ward 74', 'Amer Fort Town & Kunda Village', 7),

-- Kishanpole Zone (Wards 81 - 84)
(34, 'Ward 81', 'Chaura Rasta & Golcha Cinema Road', 8),
(35, 'Ward 82', 'Tripolia Bazaar & City Palace North Lane', 8),
(36, 'Ward 83', 'Ajmeri Gate & New Gate Vegetable Market', 8),
(37, 'Ward 84', 'C-Scheme Ashok Nagar & Statue Circle', 8);

-- -------------------------------------------------------
-- 4. SEED 5 OFFICIAL DEMO COLLECTION CREWS
-- -------------------------------------------------------
INSERT INTO crews (id, crew_name, leader_name, members_count, members_description, phone, area_id, ward_id, ward_coverage, vehicle_number, vehicle_capacity, current_workload, availability, status, user_id) VALUES
(1, 'Mansarovar Eco-Cleaners', 'Ramesh Sharma', 4, '4 Sanitation Workers + 1 Compactor Driver', '9828011111', 1, 1, 'Wards 43 to 47 (Complete Mansarovar Zone)', 'RJ-14-EA-1001', 3.50, 2, 'AVAILABLE', 'ACTIVE', 2),
(2, 'Malviya Sanitation Squad', 'Dinesh Meena', 5, '5 Bio-hazard & Waste Technicians', '9828022222', 2, 6, 'Wards 51 to 55 (Complete Malviya Nagar Zone)', 'RJ-14-GA-2001', 5.00, 2, 'AVAILABLE', 'ACTIVE', 3),
(3, 'Sanganer Rapid Waste Force', 'Suresh Yadav', 4, '4 Mechanical Lifter Operators', '9828033333', 3, 11, 'Wards 61 to 65 (Complete Sanganer Zone)', 'RJ-14-SN-3001', 4.00, 1, 'AVAILABLE', 'ACTIVE', 4),
(4, 'Civil Lines VIP Sanitation Team', 'Manoj Verma', 3, '3 Elite Government Beat Cleaners', '9828044444', 4, 16, 'Wards 15 to 19 (Complete Civil Lines Zone)', 'RJ-14-CL-4001', 2.50, 1, 'AVAILABLE', 'ACTIVE', 5),
(5, 'Vidyadhar Nagar Clean Force', 'Mukesh Gurjar', 4, '4 Commercial Zone Clearing Staff', '9828055555', 5, 21, 'Wards 1 to 5 (Complete Vidyadhar Nagar Zone)', 'RJ-14-VN-5001', 4.50, 1, 'AVAILABLE', 'ACTIVE', 6);

-- -------------------------------------------------------
-- 5. SEED COMPLAINTS (Realistic Jaipur Municipal Complaints)
-- -------------------------------------------------------
INSERT INTO complaints (id, complaint_code, citizen_user_id, citizen_name, phone, email, area_id, ward_id, locality, address, category, description, status, priority, priority_score, priority_reason, is_health_risk, is_garbage_overflow, is_sensitive_area, assigned_crew_id, expected_resolution_time, resolution_notes, created_at, resolved_at) VALUES
-- Critical & High in Mansarovar
(1, 'CMP-2026-1001', 7, 'Rahul Verma', '9876543210', 'rahul@example.com', 1, 1, 'Sector 3 Community Park', 'Corner of Shipra Path, Opp Primary School Gate', 'GARBAGE_OVERFLOW', 'Massive municipal dump bin overflowing onto school footpath. Stray animals scavenging.', 'ASSIGNED', 'CRITICAL', 95, 'Garbage overflow (+25) • Public health risk hazard (+30) • Sensitive public zone (+15) • Base severity (+25)', TRUE, TRUE, TRUE, 1, DATE_ADD(NOW(), INTERVAL 4 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NULL),
(2, 'CMP-2026-1002', 8, 'Priya Sharma', '9876543211', 'priya.s@example.com', 1, 2, 'Madhyam Marg Metro Pillar 68', 'Service lane beside metro staircase', 'DEAD_ANIMAL_WASTE', 'Dead stray cow on service lane creating severe odor and transit block.', 'IN_PROGRESS', 'CRITICAL', 92, 'Dead animal carcass (+30) • Public health risk (+30) • Metro transit corridor (+15) • Base (+15)', TRUE, FALSE, TRUE, 1, DATE_ADD(NOW(), INTERVAL 2 HOUR), 'Dispatched compactor and bleaching team.', DATE_SUB(NOW(), INTERVAL 4 HOUR), NULL),

-- Critical & High in Malviya Nagar
(3, 'CMP-2026-1003', 9, 'Amit Patel', '9876543212', 'amit.patel@example.com', 2, 6, 'Calgiri Hospital Perimeter', 'Calgiri Road Backgate Drainage Line', 'DRAINAGE_WASTE_ISSUE', 'Blocked sewer drain overflowing with hospital waste sludge into public road.', 'ASSIGNED', 'CRITICAL', 90, 'Drainage sewage contamination (+25) • Hospital sensitive zone (+15) • Public health hazard (+30)', TRUE, TRUE, TRUE, 2, DATE_ADD(NOW(), INTERVAL 3 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL),
(4, 'CMP-2026-1004', 10, 'Sneha Gupta', '9876543213', 'sneha.g@example.com', 2, 7, 'Sector 3 Central Market', 'Near SBI ATM & Sabzi Mandi lane', 'GARBAGE_OVERFLOW', 'Market community wet waste bins overturned and spilling across 30 meters.', 'IN_PROGRESS', 'HIGH', 82, 'Garbage overflow (+25) • Market footfall zone (+15) • Base severity (+25)', TRUE, TRUE, FALSE, 2, DATE_ADD(NOW(), INTERVAL 5 HOUR), 'Replacement bins deployed; sweeping underway.', DATE_SUB(NOW(), INTERVAL 5 HOUR), NULL),

-- Sanganer Zone
(5, 'CMP-2026-1005', 11, 'Vikram Singh', '9876543214', 'vikram.s@example.com', 3, 11, 'Sanganer Town Handloom Beat', 'Paper Colony Main Culvert', 'ILLEGAL_DUMPING', 'Textile chemical sludge sacks dumped overnight in public culvert.', 'ASSIGNED', 'HIGH', 78, 'Illegal dumping (+20) • Drainage obstruction (+25) • Environmental pollution (+20)', TRUE, FALSE, FALSE, 3, DATE_ADD(NOW(), INTERVAL 8 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 6 HOUR), NULL),

-- Civil Lines Zone
(6, 'CMP-2026-1006', 7, 'Rahul Verma', '9876543210', 'rahul@example.com', 4, 16, 'Raj Bhavan Perimeter', 'Bungalow 14 lane approach', 'ILLEGAL_DUMPING', 'Construction gravel and broken concrete bricks dumped on footpath.', 'ASSIGNED', 'HIGH', 75, 'Illegal dumping (+20) • VIP Security zone (+15) • Pedestrian safety (+20)', FALSE, FALSE, TRUE, 4, DATE_ADD(NOW(), INTERVAL 6 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 4 HOUR), NULL),

-- Vidyadhar Nagar Zone
(7, 'CMP-2026-1007', 8, 'Priya Sharma', '9876543211', 'priya.s@example.com', 5, 21, 'Central Spine Commercial Walk', 'Behind National Handloom Complex', 'PLASTIC_WASTE', 'Non-biodegradable single-use plastic sacks left in open vacant plot.', 'ASSIGNED', 'MEDIUM', 55, 'Plastic accumulation (+15) • Commercial zone (+15) • Base (+15)', FALSE, FALSE, FALSE, 5, DATE_ADD(NOW(), INTERVAL 14 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 7 HOUR), NULL),

-- Tickets in Zones where NO CREW is currently seeded (Tests PENDING_ASSIGNMENT feature!)
(8, 'CMP-2026-1008', 9, 'Amit Patel', '9876543212', 'amit.patel@example.com', 6, 26, 'Raja Park Main Market', 'Gali 4 intersection outside sweet shop', 'GARBAGE_OVERFLOW', 'Restaurant food waste piled up on corner; requires dedicated Moti Dungri collection crew.', 'PENDING_ASSIGNMENT', 'HIGH', 75, 'Garbage overflow (+25) • Commercial food hygiene (+30) • Base (+15)', TRUE, TRUE, FALSE, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL),
(9, 'CMP-2026-1009', 10, 'Sneha Gupta', '9876543213', 'sneha.g@example.com', 7, 30, 'Badi Chaupar Heritage Walk', 'Opposite Hawamahal tourist entry', 'STREET_CLEANLINESS', 'Tourist litter and cardboard boxes scattered along pedestrian heritage corridor.', 'PENDING_ASSIGNMENT', 'MEDIUM', 50, 'Tourist heritage area (+15) • Street cleanliness (+15) • Base (+15)', FALSE, FALSE, TRUE, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL),
(10, 'CMP-2026-1010', 11, 'Vikram Singh', '9876543214', 'vikram.s@example.com', 8, 34, 'Chaura Rasta Book Market', 'Golcha cinema side alleyway', 'CONSTRUCTION_WASTE', 'Shop renovation tiles and plaster rubble left on road.', 'PENDING_ASSIGNMENT', 'MEDIUM', 45, 'Construction debris (+20) • Walled city corridor (+15)', FALSE, FALSE, TRUE, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NULL),

-- Resolved Complaints with full historical log
(11, 'CMP-2026-1011', 7, 'Rahul Verma', '9876543210', 'rahul@example.com', 1, 1, 'Sector 2 Ring Road', 'Opposite Auto Stand', 'GARBAGE_OVERFLOW', 'Dustbin overflow near auto stand causing traffic bottlenecks.', 'RESOLVED', 'HIGH', 76, 'Traffic disruption (+20) • Overflowing community dustbin (+25)', FALSE, TRUE, FALSE, 1, DATE_SUB(NOW(), INTERVAL 18 HOUR), 'Compactor truck cleared 2.2 tons of waste and sanitized site with bleaching powder.', DATE_SUB(NOW(), INTERVAL 24 HOUR), DATE_SUB(NOW(), INTERVAL 18 HOUR)),
(12, 'CMP-2026-1012', 8, 'Priya Sharma', '9876543211', 'priya.s@example.com', 2, 6, 'Model Town Gali 7', 'Row houses 45 to 60', 'GARBAGE_NOT_COLLECTED', 'Waste pickup cart missed colony row houses.', 'RESOLVED', 'LOW', 35, 'Residential missed pickup (+15) • Low severity (+10)', FALSE, FALSE, FALSE, 2, DATE_SUB(NOW(), INTERVAL 30 HOUR), 'Assigned auxiliary collection van to complete row house clearance.', DATE_SUB(NOW(), INTERVAL 36 HOUR), DATE_SUB(NOW(), INTERVAL 30 HOUR)),
(13, 'CMP-2026-1013', 9, 'Amit Patel', '9876543212', 'amit.patel@example.com', 4, 18, 'Sodala Sabzi Mandi Road', 'Outer bypass culvert', 'ILLEGAL_DUMPING', 'Rotten vegetable crates dumped into public water channel.', 'RESOLVED', 'HIGH', 79, 'Organic decay health hazard (+30) • Public market channel (+20)', TRUE, FALSE, FALSE, 4, DATE_SUB(NOW(), INTERVAL 20 HOUR), 'Cleared 3.1 tons of rotten produce and issued notice to vegetable association.', DATE_SUB(NOW(), INTERVAL 28 HOUR), DATE_SUB(NOW(), INTERVAL 20 HOUR));

-- -------------------------------------------------------
-- 6. SEED COMPLAINT ASSIGNMENTS
-- -------------------------------------------------------
INSERT INTO complaint_assignments (complaint_id, crew_id, assigned_by_user_id, notes) VALUES
(1, 1, 1, 'Smart Routing Engine auto-dispatched to Mansarovar Eco-Cleaners'),
(2, 1, 1, 'Emergency animal carcass priority dispatch to Crew 1'),
(3, 2, 1, 'Hospital drainage contamination crew assigned'),
(4, 2, 1, 'Commercial market overflow assigned to Crew 2'),
(5, 3, 1, 'Sanganer chemical sludge investigation & lifting team'),
(6, 4, 1, 'Civil Lines VIP clearance team dispatched'),
(7, 5, 1, 'Vidyadhar Nagar plot cleanup team'),
(11, 1, 1, 'Routine Mansarovar morning clearance'),
(12, 2, 1, 'Model Town auxiliary clearance'),
(13, 4, 1, 'Sodala vegetable channel clearance team');

-- -------------------------------------------------------
-- 7. SEED STATUS HISTORY
-- -------------------------------------------------------
INSERT INTO complaint_status_history (complaint_id, old_status, new_status, changed_by_user_id, comments) VALUES
(1, 'NEW', 'ASSIGNED', 1, 'Auto-assigned to Mansarovar Eco-Cleaners (RJ-14-EA-1001)'),
(2, 'NEW', 'ASSIGNED', 1, 'Critical priority dispatch to Crew 1'),
(2, 'ASSIGNED', 'IN_PROGRESS', 2, 'Crew arrived on site with carcass vehicle and disinfectant'),
(3, 'NEW', 'ASSIGNED', 1, 'Assigned to Malviya Sanitation Squad'),
(4, 'NEW', 'ASSIGNED', 1, 'Assigned to Malviya Sanitation Squad'),
(4, 'ASSIGNED', 'IN_PROGRESS', 3, 'Replacement bins brought on site'),
(8, 'NEW', 'PENDING_ASSIGNMENT', NULL, 'No active crew currently stationed in Moti Dungri Zone; Admin notified'),
(9, 'NEW', 'PENDING_ASSIGNMENT', NULL, 'No active crew currently stationed in Hawa Mahal Zone; Admin notified'),
(10, 'NEW', 'PENDING_ASSIGNMENT', NULL, 'No active crew currently stationed in Kishanpole Zone; Admin notified'),
(11, 'NEW', 'ASSIGNED', 1, 'Assigned to Crew 1'),
(11, 'ASSIGNED', 'IN_PROGRESS', 2, 'Compactor operation commenced'),
(11, 'IN_PROGRESS', 'RESOLVED', 2, 'Site completely cleared and sanitized');

-- -------------------------------------------------------
-- 8. SEED FEEDBACK
-- -------------------------------------------------------
INSERT INTO feedback (complaint_id, citizen_user_id, rating, comments) VALUES
(11, 7, 5, 'Rapid response! The large dustbin overflow near auto stand was cleared within 6 hours. Excellent work by Nagar Nigam Jaipur.'),
(12, 8, 4, 'Door-to-door van arrived in afternoon to complete remaining row houses.'),
(13, 9, 5, 'The foul smell from rotten vegetables is completely eliminated. Sanitizer was sprayed.');

-- -------------------------------------------------------
-- 9. SEED NOTIFICATIONS
-- -------------------------------------------------------
INSERT INTO notifications (user_id, role_target, title, message) VALUES
(1, 'ADMIN', 'Pending Assignment Alert', 'Complaint CMP-2026-1008 in Raja Park Market has no assigned zone crew. Manual dispatch needed.'),
(1, 'ADMIN', 'Pending Assignment Alert', 'Complaint CMP-2026-1009 in Hawa Mahal heritage zone requires special crew deployment.'),
(2, 'CREW', 'Critical Task Dispatched', 'CMP-2026-1001: Severe garbage overflow near primary school in Ward 43.'),
(7, 'CITIZEN', 'Complaint Resolved', 'Your complaint CMP-2026-1011 has been resolved by Mansarovar Eco-Cleaners. Please rate our service.');

-- -------------------------------------------------------
-- 10. SEED AUDIT LOGS
-- -------------------------------------------------------
INSERT INTO audit_logs (user_id, username, action, target_type, target_id, details) VALUES
(1, 'admin', 'LOGIN', 'USER', 1, 'Administrator logged in to Smart City Control Center'),
(1, 'admin', 'SYSTEM_INITIALIZATION', 'DATABASE', NULL, 'Database schema and official Jaipur municipal seed data initialized'),
(7, 'rahul', 'COMPLAINT_CREATED', 'COMPLAINT', 1, 'Citizen registered complaint CMP-2026-1001 (Priority: CRITICAL, Score: 95)'),
(1, 'SYSTEM', 'CREW_ASSIGNED', 'COMPLAINT', 1, 'Smart Routing auto-assigned CMP-2026-1001 to Mansarovar Eco-Cleaners'),
(2, 'crew_mansarovar', 'STATUS_CHANGED', 'COMPLAINT', 11, 'Status updated to RESOLVED with notes');
