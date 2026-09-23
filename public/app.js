/**
 * CardioConnect - Clinical ECG & Telemetry Engine
 * Real-time Canvas Waveform, BLE Telemetry, Demo Generator, Session History & Export
 */

(function () {
    // State
    const state = {
        isDemoMode: true,
        isBleConnected: false,
        isPaused: false,
        isRecording: false,
        recordingStartTime: null,
        recordingTimerInterval: null,
        recordedDurationSeconds: 0,
        recordedSamples: [],
        recordedMarkers: [],
        recordedHeartRates: [],
        
        // Vitals
        heartRate: 78,
        avgHeartRate: 76,
        minHeartRate: 68,
        maxHeartRate: 88,
        spo2: 98,
        bpSys: 120,
        bpDia: 80,
        tempC: 36.7,
        battery: 94,
        rssi: -54,
        deviceName: 'Simulated CardioConnect Pro',
        signalQuality: 'GOOD',
        rhythm: 'Normal Sinus Pattern (Validated by Device)',

        // ECG Calibration
        selectedLead: 'LEAD_II',
        sweepSpeed: 25, // mm/s
        gain: 10, // mm/mV
        showGrid: true,
        // Multi-Patient Registry
        patients: [
            {
                patientId: 'P-10245',
                fullName: 'Rahul Sharma',
                age: 52,
                gender: 'Male',
                dob: '14 May 1974',
                bloodGroup: 'B+',
                heightCm: 175,
                weightKg: 78,
                phone: '+91 98765 43210',
                emergency: 'Sunita Sharma (+91 98765 43211)',
                clinician: 'Dr. Amit Verma (Cardiology)',
                allergies: 'Penicillin (rash)',
                medications: 'Ramipril 5mg OD, Atorvastatin 10mg HS',
                notes: 'Mild hypertension managed with ACE inhibitors. No prior myocardial infarction.',
                totalSessions: 3,
                lastEcgTime: 'Today, 10:32 PM'
            },
            {
                patientId: 'P-10246',
                fullName: 'Priya Verma',
                age: 45,
                gender: 'Female',
                dob: '22 August 1981',
                bloodGroup: 'O+',
                heightCm: 162,
                weightKg: 61,
                phone: '+91 98765 43212',
                emergency: 'Rohan Verma (+91 98765 43213)',
                clinician: 'Dr. Amit Verma (Cardiology)',
                allergies: 'No known drug allergies',
                medications: 'None',
                notes: 'Occasional palpitations during physical exertion. Normal echocardiogram.',
                totalSessions: 1,
                lastEcgTime: 'Yesterday, 04:15 PM'
            },
            {
                patientId: 'P-10247',
                fullName: 'Vikram Singh',
                age: 64,
                gender: 'Male',
                dob: '03 November 1961',
                bloodGroup: 'A+',
                heightCm: 170,
                weightKg: 82,
                phone: '+91 98765 43214',
                emergency: 'Anita Singh (+91 98765 43215)',
                clinician: 'Dr. S. K. Gupta',
                allergies: 'Sulfa drugs',
                medications: 'Metformin 500mg, Clopidogrel 75mg, Metoprolol 25mg',
                notes: 'Type 2 Diabetes, post-PCI stent placed 2024. Routine outpatient telemetry.',
                totalSessions: 5,
                lastEcgTime: '21 Sept, 11:20 AM'
            }
        ],
        activePatientId: 'P-10245',
        patient: {
            fullName: 'Rahul Sharma',
            patientId: 'P-10245',
            age: 52,
            gender: 'Male',
            dob: '14 May 1974',
            bloodGroup: 'B+',
            heightCm: 175,
            weightKg: 78,
            phone: '+91 98765 43210',
            emergency: 'Sunita Sharma (+91 98765 43211)',
            clinician: 'Dr. Amit Verma (Cardiology)',
            allergies: 'Penicillin (rash)',
            medications: 'Ramipril 5mg OD, Atorvastatin 10mg HS',
            notes: 'Mild hypertension managed with ACE inhibitors. No prior myocardial infarction.',
            totalSessions: 3,
            lastEcgTime: 'Today, 10:32 PM'
        },

        // Interval measurements
        measurements: {
            prInterval: 160,
            qrsDuration: 92,
            qtInterval: 380,
            qtc: 410,
            rrInterval: 770,
            pWave: 'Normal upright in Lead II',
            qrsMorph: 'Narrow QRS (<120 ms)',
            stSegment: 'Isoelectric (no acute deviation)',
            regularity: 'Regular'
        },

        // ECG Interpretation & Risk
        interpretation: {
            riskStatus: 'normal',
            riskLabel: '🟢 No significant device/algorithm finding',
            rhythm: 'Normal Sinus Rhythm',
            algorithm: 'Validated Automated Classifier v2.4',
            findings: []
        },

        // Sessions Database (stored in localStorage)
        sessions: []
    };

    // Buffer for ECG Rendering
    const BUFFER_SIZE = 600;
    const voltageBuffer = new Array(BUFFER_SIZE).fill(0);
    let sweepPointer = 0;

    // Canvas elements
    const canvasDash = document.getElementById('ecgCanvasDashboard');
    const canvasLive = document.getElementById('ecgCanvasLive');
    const canvasReview = document.getElementById('ecgCanvasReview');

    let ctxDash = canvasDash ? canvasDash.getContext('2d') : null;
    let ctxLive = canvasLive ? canvasLive.getContext('2d') : null;
    let ctxReview = canvasReview ? canvasReview.getContext('2d') : null;

    // Initialize initial seed session if none
    function initStorage() {
        const stored = localStorage.getItem('cardioconnect_sessions');
        if (stored) {
            try {
                state.sessions = JSON.parse(stored);
            } catch (e) {
                state.sessions = [];
            }
        }
        if (!state.sessions || state.sessions.length === 0) {
            // Seed baseline clinical session
            const seedSamples = [];
            for (let i = 0; i < 1200; i++) {
                const phase = (i % 200) / 200.0;
                const p = 0.16 * Math.exp(-Math.pow(phase - 0.20, 2) / 0.0008);
                const q = -0.18 * Math.exp(-Math.pow(phase - 0.35, 2) / 0.0001);
                const r = 1.35 * Math.exp(-Math.pow(phase - 0.38, 2) / 0.0002);
                const s = -0.32 * Math.exp(-Math.pow(phase - 0.41, 2) / 0.0002);
                const t = 0.28 * Math.exp(-Math.pow(phase - 0.65, 2) / 0.0025);
                seedSamples.push(Number((p + q + r + s + t).toFixed(3)));
            }

            state.sessions = [{
                id: Date.now() - 14400000,
                title: 'Baseline Clinical ECG Session',
                date: new Date(Date.now() - 14400000).toLocaleString(),
                durationSeconds: 342,
                avgHeartRate: 76,
                minHeartRate: 68,
                maxHeartRate: 88,
                lead: 'Lead II',
                deviceName: 'CardioConnect Pro-12',
                spo2: 98,
                bp: '120/80',
                temp: 36.7,
                isDemo: true,
                samples: seedSamples,
                markers: [{ timeOffset: '01:15', note: 'Resting Baseline', hr: 75 }]
            }];
            saveSessions();
        }
        renderHistoryList();

        // Load multi-patient storage
        const storedPatients = localStorage.getItem('cardioconnect_patients');
        if (storedPatients) {
            try {
                const list = JSON.parse(storedPatients);
                if (Array.isArray(list) && list.length > 0) state.patients = list;
            } catch (e) {}
        }
        const activeId = localStorage.getItem('cardioconnect_active_patient_id');
        if (activeId && state.patients.some(p => p.patientId === activeId)) {
            state.activePatientId = activeId;
        } else if (state.patients.length > 0) {
            state.activePatientId = state.patients[0].patientId;
        }
        state.patient = getActivePatient();
        renderPatientsList();
    }

    function saveSessions() {
        localStorage.setItem('cardioconnect_sessions', JSON.stringify(state.sessions));
        renderHistoryList();
    }

    function getActivePatient() {
        return state.patients.find(p => p.patientId === state.activePatientId) || state.patients[0] || state.patient;
    }

    function savePatients() {
        localStorage.setItem('cardioconnect_patients', JSON.stringify(state.patients));
        localStorage.setItem('cardioconnect_active_patient_id', state.activePatientId);
        updatePatientUI();
        renderPatientsList();
    }

    function getNextSuggestedPatientId() {
        let maxNum = 10245;
        state.patients.forEach(p => {
            const numStr = (p.patientId || '').replace(/[^0-9]/g, '');
            const num = parseInt(numStr, 10);
            if (!isNaN(num) && num > maxNum) maxNum = num;
        });
        return `P-${maxNum + 1}`;
    }

    function calculateBmi(heightCm, weightKg) {
        if (!heightCm || !weightKg || heightCm <= 0 || weightKg <= 0) return { bmi: 0, text: '—', cat: 'N/A' };
        const hM = heightCm / 100;
        const val = +(weightKg / (hM * hM)).toFixed(1);
        let cat = 'Normal weight';
        if (val < 18.5) cat = 'Underweight';
        else if (val >= 25 && val < 30) cat = 'Overweight';
        else if (val >= 30) cat = 'Obese';
        return { bmi: val, text: `${val} (${cat})`, cat };
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str).replace(/[&<>"']/g, m => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        }[m]));
    }

    function getInitials(name) {
        if (!name) return 'P';
        const parts = name.trim().split(/\s+/);
        if (parts.length >= 2) {
            return (parts[0][0] + parts[1][0]).toUpperCase();
        }
        return (name.substring(0, 2) || 'P').toUpperCase();
    }

    function switchTab(tabId) {
        const navButtons = document.querySelectorAll('.nav-item');
        const tabPanes = document.querySelectorAll('.tab-pane');
        navButtons.forEach(b => {
            if (b.getAttribute('data-tab') === tabId) b.classList.add('active');
            else b.classList.remove('active');
        });
        tabPanes.forEach(p => {
            if (p.id === tabId) p.classList.add('active');
            else p.classList.remove('active');
        });
    }

    // Mathematical Cardiac P-Q-R-S-T Signal Generator
    let cardiacPhase = 0;
    let respirationPhase = 0;
    let hrAccumulator = 0;
    let hrCount = 0;

    function stepSyntheticEcg() {
        if (!state.isDemoMode && !state.isBleConnected) {
            // Flatline or zero if disconnected and demo off
            return 0;
        }

        const baseBpm = 75;
        // Respiratory Sinus Arrhythmia
        const rsa = 3.5 * Math.sin(respirationPhase);
        const instantBpm = baseBpm + rsa + (Math.random() - 0.5) * 1.5;

        const sampleRate = 250;
        const cycleDurationSec = 60.0 / instantBpm;
        const phaseIncrement = (1.0 / sampleRate) / cycleDurationSec;

        cardiacPhase += phaseIncrement;
        if (cardiacPhase >= 1.0) {
            cardiacPhase -= 1.0;
            const currentHr = Math.round(instantBpm);
            state.heartRate = currentHr;
            state.minHeartRate = Math.min(state.minHeartRate, currentHr);
            state.maxHeartRate = Math.max(state.maxHeartRate, currentHr);
            hrAccumulator += currentHr;
            hrCount++;
            state.avgHeartRate = Math.round(hrAccumulator / hrCount);

            updateVitalsUI();
        }

        respirationPhase += (2 * Math.PI * 0.25) / sampleRate;
        const baselineWander = 0.04 * Math.sin(respirationPhase);
        const muscleNoise = (Math.random() - 0.5) * 0.015;

        // Gaussian wave formula
        function gaussian(amp, center, width) {
            const diff = cardiacPhase - center;
            return amp * Math.exp(-(diff * diff) / (2.0 * width * width));
        }

        const pWave = gaussian(0.16, 0.20, 0.028);
        const qWave = gaussian(-0.18, 0.35, 0.010);
        const rWave = gaussian(1.35, 0.38, 0.012);
        const sWave = gaussian(-0.32, 0.41, 0.013);
        const tWave = gaussian(0.28, 0.65, 0.048);
        const uWave = gaussian(0.03, 0.78, 0.030);

        const mv = pWave + qWave + rWave + sWave + tWave + uWave + baselineWander + muscleNoise;
        return mv;
    }

    // Draw ECG Medical Grid
    function drawMedicalGrid(ctx, width, height) {
        if (!state.showGrid) return;

        const minorSpacing = 16; // 1mm equivalent
        const majorSpacing = minorSpacing * 5; // 5mm major block

        ctx.lineWidth = 0.5;
        ctx.strokeStyle = '#0E1724';

        // Minor vertical
        for (let x = 0; x <= width; x += minorSpacing) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, height);
            ctx.stroke();
        }

        // Minor horizontal
        for (let y = 0; y <= height; y += minorSpacing) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(width, y);
            ctx.stroke();
        }

        // Major lines
        ctx.lineWidth = 1.0;
        ctx.strokeStyle = '#19273B';

        for (let x = 0; x <= width; x += majorSpacing) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, height);
            ctx.stroke();
        }

        const centerY = height / 2;
        for (let y = centerY % majorSpacing; y <= height; y += majorSpacing) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(width, y);
            ctx.stroke();
        }
    }

    // Draw 1 mV square calibration pulse at left
    function drawCalibPulse(ctx, left, centerY, mvScale) {
        ctx.save();
        ctx.strokeStyle = 'rgba(0, 230, 118, 0.4)';
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.moveTo(left, centerY);
        ctx.lineTo(left + 5, centerY);
        ctx.lineTo(left + 5, centerY - mvScale);
        ctx.lineTo(left + 16, centerY - mvScale);
        ctx.lineTo(left + 16, centerY);
        ctx.lineTo(left + 22, centerY);
        ctx.stroke();
        ctx.restore();
    }

    // Render Canvas Frame
    function renderEcgWaveform(ctx, canvas) {
        if (!ctx || !canvas) return;

        const width = canvas.width = canvas.clientWidth * window.devicePixelRatio;
        const height = canvas.height = canvas.clientHeight * window.devicePixelRatio;
        const centerY = height / 2;

        ctx.fillStyle = '#04070B';
        ctx.fillRect(0, 0, width, height);

        drawMedicalGrid(ctx, width, height);

        const mvScale = (height * 0.32) * (state.gain / 10.0);
        const calibWidth = 35 * window.devicePixelRatio;
        drawCalibPulse(ctx, 10 * window.devicePixelRatio, centerY, mvScale);

        const renderLeft = 10 * window.devicePixelRatio + calibWidth + (10 * window.devicePixelRatio);
        const renderWidth = width - renderLeft;
        const stepX = renderWidth / (BUFFER_SIZE - 1);

        ctx.strokeStyle = state.traceColor;
        ctx.lineWidth = 2.4 * window.devicePixelRatio;
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';

        ctx.beginPath();
        let pathStarted = false;

        for (let i = 0; i < BUFFER_SIZE; i++) {
            const mv = voltageBuffer[i];
            const px = renderLeft + (i * stepX);

            if (isNaN(mv)) {
                pathStarted = false;
                continue;
            }

            const py = centerY - (mv * mvScale);
            const clampedY = Math.max(4, Math.min(height - 4, py));

            if (!pathStarted) {
                ctx.moveTo(px, clampedY);
                pathStarted = true;
            } else {
                ctx.lineTo(px, clampedY);
            }
        }
        ctx.stroke();

        // Draw sweeping scanner beam
        if (!state.isPaused) {
            const sweepX = renderLeft + (sweepPointer * stepX);
            ctx.fillStyle = 'rgba(0, 230, 118, 0.35)';
            ctx.fillRect(sweepX, 0, 2 * window.devicePixelRatio, height);
        }
    }

    // Animation Loop
    let lastSampleTime = performance.now();
    function animationLoop(timestamp) {
        // Generate samples at ~250 Hz rate
        const delta = timestamp - lastSampleTime;
        const sampleInterval = 1000 / 250; // 4ms
        const samplesToGen = Math.min(Math.floor(delta / sampleInterval), 15);

        if (!state.isPaused && samplesToGen > 0) {
            for (let s = 0; s < samplesToGen; s++) {
                const mv = stepSyntheticEcg();
                voltageBuffer[sweepPointer] = mv;

                // Erase beam ahead
                const eraseAhead = 8;
                for (let k = 1; k <= eraseAhead; k++) {
                    const eraseIdx = (sweepPointer + k) % BUFFER_SIZE;
                    voltageBuffer[eraseIdx] = NaN;
                }

                sweepPointer = (sweepPointer + 1) % BUFFER_SIZE;

                // Recording
                if (state.isRecording) {
                    state.recordedSamples.push(Number(mv.toFixed(3)));
                }
            }
            lastSampleTime = timestamp;
        }

        renderEcgWaveform(ctxDash, canvasDash);
        renderEcgWaveform(ctxLive, canvasLive);

        requestAnimationFrame(animationLoop);
    }

    // UI Updates
    function updateVitalsUI() {
        document.getElementById('hrValue').innerText = state.heartRate;
        document.getElementById('hrAvg').innerText = `${state.avgHeartRate} bpm`;
        document.getElementById('hrMin').innerText = `${state.minHeartRate} bpm`;
        document.getElementById('hrMax').innerText = `${state.maxHeartRate} bpm`;

        const liveHr = document.getElementById('liveHr');
        if (liveHr) liveHr.innerText = state.heartRate;

        document.getElementById('spo2Val').innerText = state.spo2;
        const liveSpo2 = document.getElementById('liveSpo2');
        if (liveSpo2) liveSpo2.innerText = state.spo2;

        document.getElementById('bpVal').innerText = `${state.bpSys}/${state.bpDia}`;
        const liveBp = document.getElementById('liveBp');
        if (liveBp) liveBp.innerText = `${state.bpSys}/${state.bpDia}`;

        document.getElementById('tempVal').innerText = state.tempC.toFixed(1);
        document.getElementById('batteryVal').innerText = `${state.battery}%`;

        // Signal Quality
        const signalBadge = document.getElementById('signalBadge');
        const signalText = document.getElementById('signalText');
        const liveSignal = document.getElementById('liveSignal');

        signalText.innerText = `${state.signalQuality} SIGNAL`;
        if (liveSignal) liveSignal.innerText = state.signalQuality;

        if (state.signalQuality === 'GOOD') {
            signalBadge.className = 'signal-chip good';
        } else if (state.signalQuality === 'WEAK') {
            signalBadge.className = 'signal-chip warning';
        } else {
            signalBadge.className = 'signal-chip danger';
        }

        // Device Badge
        const statusBadge = document.getElementById('deviceStatusBadge');
        const statusText = document.getElementById('deviceStatusText');

        if (state.isDemoMode) {
            statusBadge.className = 'status-pill demo-mode';
            statusText.innerText = 'DEMO DATA — NOT REAL ECG';
        } else if (state.isBleConnected) {
            statusBadge.className = 'status-pill connected';
            statusText.innerText = '● ECG CONNECTED';
        } else {
            statusBadge.className = 'status-pill danger';
            statusText.innerText = 'DISCONNECTED';
        }

        updatePatientUI();
        updateInterpretationUI();
    }

    // Patient Profile UI Update
    function updatePatientUI() {
        const p = getActivePatient();
        state.patient = p;
        const patName = document.getElementById('patName');
        if (patName) patName.innerText = p.fullName;

        const patDemographics = document.getElementById('patDemographics');
        if (patDemographics) patDemographics.innerText = `${p.age}y • ${p.gender}`;

        const patId = document.getElementById('patId');
        if (patId) patId.innerText = `ID: ${p.patientId}`;

        const patBlood = document.getElementById('patBlood');
        if (patBlood) patBlood.innerText = `Blood: ${p.bloodGroup || '—'}`;

        const patClinician = document.getElementById('patClinician');
        if (patClinician) patClinician.innerText = p.clinician || 'Unassigned';
    }

    let pendingSwitchPatientId = null;
    let viewingProfilePatientId = null;

    function renderPatientsList(query = '') {
        const container = document.getElementById('patientCardsContainer');
        const sub = document.getElementById('patientCountSubtitle');
        if (!container) return;

        const activeP = getActivePatient();
        if (sub) {
            sub.innerText = `${state.patients.length} Registered Patients • Active: ${activeP.fullName}`;
        }

        let filtered = state.patients;
        if (query && query.trim() !== '') {
            const q = query.trim().toLowerCase();
            filtered = state.patients.filter(p =>
                (p.fullName && p.fullName.toLowerCase().includes(q)) ||
                (p.patientId && p.patientId.toLowerCase().includes(q)) ||
                (p.phone && p.phone.toLowerCase().includes(q)) ||
                (p.clinician && p.clinician.toLowerCase().includes(q))
            );
        }

        if (filtered.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 40px 20px; color: var(--text-tertiary);">
                    <div style="font-size: 36px; margin-bottom: 8px;">👤</div>
                    <p style="font-size: 14px; color: var(--text-secondary);">No patients found matching "${escapeHtml(query)}"</p>
                </div>
            `;
            return;
        }

        container.innerHTML = filtered.map(p => {
            const isActive = p.patientId === state.activePatientId;
            const initials = getInitials(p.fullName);
            return `
                <div class="patient-dir-card ${isActive ? 'active' : ''}" data-id="${p.patientId}">
                    <div class="pat-card-header">
                        <div class="pat-identity">
                            <div class="pat-initials-avatar">${initials}</div>
                            <div class="pat-info-col">
                                <div class="pat-title-line">
                                    <strong class="pat-name-text">${escapeHtml(p.fullName)}</strong>
                                    ${isActive ? '<span class="pat-check-icon">✓</span>' : ''}
                                </div>
                                <span class="pat-sub-line">ID: ${p.patientId} • ${p.age}y, ${p.gender}</span>
                            </div>
                        </div>
                        <span class="pat-status-pill ${isActive ? 'active' : 'idle'}">${isActive ? 'ACTIVE' : 'IDLE'}</span>
                    </div>

                    <div class="pat-meta-bar">
                        <span>Last ECG: ${p.lastEcgTime || 'Never'}</span>
                        <span>Blood: ${p.bloodGroup || '—'} • ${p.totalSessions || 0} Sessions</span>
                    </div>

                    <div class="pat-actions-row">
                        <button class="pill-btn ${isActive ? 'primary-btn' : 'secondary-btn'} pat-monitor-btn" data-id="${p.patientId}">
                            ${isActive ? '● Live Monitor' : 'Monitor Patient'}
                        </button>
                        <button class="pill-btn secondary-btn pat-profile-btn" data-id="${p.patientId}">
                            View Profile
                        </button>
                    </div>
                </div>
            `;
        }).join('');

        // Attach action handlers
        container.querySelectorAll('.pat-monitor-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = btn.getAttribute('data-id');
                attemptPatientSwitch(id, true);
            });
        });

        container.querySelectorAll('.pat-profile-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = btn.getAttribute('data-id');
                openPatientProfileDetailModal(id);
            });
        });
    }

    function attemptPatientSwitch(targetPatientId, navigateToLive = false) {
        if (targetPatientId === state.activePatientId) {
            if (navigateToLive) switchTab('tabLive');
            return;
        }

        const targetP = state.patients.find(p => p.patientId === targetPatientId);
        if (!targetP) return;

        if (state.isRecording) {
            // Safety Guard Triggered!
            pendingSwitchPatientId = targetPatientId;
            const curP = getActivePatient();
            document.getElementById('guardCurrentPatName').innerText = `${curP.fullName} (${curP.patientId})`;
            document.getElementById('guardTargetPatName').innerText = `${targetP.fullName} (${targetP.patientId})`;
            document.getElementById('safetyGuardModal').classList.remove('hidden');
        } else {
            // Safe to switch immediately
            executePatientSwitch(targetPatientId);
            if (navigateToLive) switchTab('tabLive');
        }
    }

    function executePatientSwitch(targetPatientId) {
        state.activePatientId = targetPatientId;
        state.patient = getActivePatient();
        savePatients();
    }

    function openPatientProfileDetailModal(id) {
        viewingProfilePatientId = id;
        const p = state.patients.find(pat => pat.patientId === id) || getActivePatient();
        const isActive = p.patientId === state.activePatientId;

        document.getElementById('profPatName').innerText = p.fullName;
        document.getElementById('profPatIdBadge').innerText = `ID: ${p.patientId} • ${p.age}y • ${p.gender} ${isActive ? '• [ACTIVE]' : ''}`;
        document.getElementById('profAgeDob').innerText = `${p.age}y (${p.dob || 'Unspecified'})`;
        document.getElementById('profBlood').innerText = p.bloodGroup || '—';
        document.getElementById('profHeightWeight').innerText = `${p.heightCm || 0} cm / ${p.weightKg || 0} kg`;
        
        const bmiInfo = calculateBmi(p.heightCm, p.weightKg);
        document.getElementById('profBmi').innerText = bmiInfo.text;
        
        document.getElementById('profPhone').innerText = p.phone || 'Unspecified';
        document.getElementById('profEmergency').innerText = p.emergency || 'Unspecified';
        document.getElementById('profClinician').innerText = p.clinician || 'Unassigned';
        document.getElementById('profAllergies').innerText = p.allergies || 'None reported';
        document.getElementById('profMeds').innerText = p.medications || 'None recorded';
        document.getElementById('profNotes').innerText = p.notes || 'None recorded';

        const setBtnText = document.getElementById('profSetMonitorBtnText');
        if (isActive) {
            setBtnText.innerText = 'Open Live Monitor';
        } else {
            setBtnText.innerText = 'Set as Active & Monitor';
        }

        document.getElementById('patientProfileDetailModal').classList.remove('hidden');
    }

    // ECG Interpretation & Interval Measurements Update
    function updateInterpretationUI() {
        const hr = state.heartRate;
        const rr = Math.round(60000 / Math.max(30, hr));
        const pr = 160;
        const qrs = 92;
        const qt = Math.round(380 * Math.sqrt(rr / 1000));
        const qtc = Math.round(qt / Math.sqrt(rr / 1000));

        state.measurements.heartRateBpm = hr;
        state.measurements.rrInterval = rr;
        state.measurements.prInterval = pr;
        state.measurements.qrsDuration = qrs;
        state.measurements.qtInterval = qt;
        state.measurements.qtc = qtc;

        // Update Measurement Elements
        const mHr = document.getElementById('measHr'); if (mHr) mHr.innerText = `${hr} BPM`;
        const mPr = document.getElementById('measPr'); if (mPr) mPr.innerText = `${pr} ms`;
        const mQrs = document.getElementById('measQrs'); if (mQrs) mQrs.innerText = `${qrs} ms`;
        const mQt = document.getElementById('measQt'); if (mQt) mQt.innerText = `${qt} ms`;
        const mQtc = document.getElementById('measQtc'); if (mQtc) mQtc.innerText = `${qtc} ms`;
        const mRr = document.getElementById('measRr'); if (mRr) mRr.innerText = `${rr} ms`;

        // Determine Findings and Risk/Attention Status
        let riskStatus = 'normal';
        let riskLabel = '🟢 No significant device/algorithm finding';
        let rhythm = 'Normal Sinus Rhythm';
        const findings = [];

        if (hr > 100) {
            riskStatus = 'attention';
            riskLabel = '🟡 Finding requires attention';
            rhythm = 'Possible Sinus Tachycardia detected by algorithm';
            findings.push({
                title: 'Possible Sinus Tachycardia',
                measured: `HR ${hr} BPM (Threshold >100 BPM)`,
                means: 'The heart rate is faster than the expected resting reference range.',
                context: 'This finding alone does not establish a diagnosis or cause. Heart rate can elevate due to physical exertion, psychological stress, fever, dehydration, caffeine, medications, or other underlying conditions.',
                action: 'If the reading is persistent or associated with concerning symptoms, seek assessment from a qualified healthcare professional.'
            });
        } else if (hr < 60) {
            riskStatus = 'attention';
            riskLabel = '🟡 Finding requires attention';
            rhythm = 'Possible Sinus Bradycardia detected by algorithm';
            findings.push({
                title: 'Possible Sinus Bradycardia',
                measured: `HR ${hr} BPM (Threshold <60 BPM)`,
                means: 'The heart rate is slower than standard resting limits while regular sinoatrial pacing is maintained.',
                context: 'Common in well-trained athletes or during deep sleep, but may also reflect medication effects, hypothermia, or conduction delays.',
                action: 'Consult a healthcare professional if accompanied by symptoms such as dizziness, lightheadedness, or unusual fatigue.'
            });
        } else {
            riskStatus = 'normal';
            riskLabel = '🟢 No significant device/algorithm finding';
            rhythm = 'Normal Sinus Rhythm';
            findings.push({
                title: 'Normal sinus rhythm',
                measured: `HR ${hr} BPM, PR ${pr} ms, QRS ${qrs} ms`,
                means: 'Electrical impulses originate regularly from the sinoatrial node within standard physiological rate and conduction limits.',
                context: 'This represents normal cardiac conduction during the recording window. A normal resting ECG does not rule out intermittent symptoms or structural heart disease.',
                action: 'Continue standard continuous clinical telemetry monitoring.'
            });
        }

        // Secondary baseline finding
        findings.push({
            title: 'Isoelectric ST-Segment',
            measured: 'ST Elevation <0.05 mV',
            means: 'No acute ST-segment elevation or depression detected relative to the PR isoelectric line.',
            context: 'ST segment shifts are critical markers that warrant prompt medical review when present.',
            action: 'Continue ongoing continuous ST-segment lead analysis.'
        });

        state.interpretation.riskStatus = riskStatus;
        state.interpretation.riskLabel = riskLabel;
        state.interpretation.rhythm = rhythm;
        state.interpretation.findings = findings;

        // Update Interpretation DOM
        const riskChip = document.getElementById('riskStatusChip');
        const riskText = document.getElementById('riskStatusLabel');
        const rhythmTitle = document.getElementById('interpRhythmTitle');

        if (riskChip) riskChip.className = `risk-chip ${riskStatus}`;
        if (riskText) riskText.innerText = riskLabel;
        if (rhythmTitle) rhythmTitle.innerText = rhythm;

        const findingsList = document.getElementById('findingsList');
        if (findingsList) {
            findingsList.innerHTML = findings.map((f, i) => `
                <div class="finding-item" onclick="window.showDiagnosticDetail(${i})">
                    <div>
                        <div class="f-title">${f.title}</div>
                        <div class="f-meas">${f.measured}</div>
                    </div>
                    <span style="color: var(--phosphor-green); font-size: 14px;">ℹ️</span>
                </div>
            `).join('');
        }
    }

    // Expose Diagnostic Modal opener to window
    window.showDiagnosticDetail = function(index) {
        const finding = state.interpretation.findings[index];
        if (!finding) return;

        document.getElementById('diagTitle').innerText = finding.title;
        document.getElementById('diagMeasured').innerText = `Measured: ${finding.measured}`;
        document.getElementById('diagMeans').innerText = finding.means;
        document.getElementById('diagContext').innerText = finding.context;
        document.getElementById('diagAction').innerText = finding.action;

        document.getElementById('diagnosticExplanationModal').classList.remove('hidden');
    };

    // Recording Controller
    function startRecording() {
        state.isRecording = true;
        state.recordingStartTime = Date.now();
        state.recordedDurationSeconds = 0;
        state.recordedSamples = [];
        state.recordedMarkers = [];
        state.recordedHeartRates = [];

        document.getElementById('recBlinker').classList.remove('hidden');
        document.getElementById('markEventBtn').classList.remove('hidden');
        document.getElementById('recordBtnLabel').innerText = 'Stop Recording';
        document.getElementById('toggleRecordBtn').className = 'pill-btn danger-btn';
        document.getElementById('liveRecBadge').classList.remove('hidden');
        document.getElementById('liveRecordBtn').innerText = 'Stop';
        document.getElementById('liveRecordBtn').className = 'pill-btn danger-btn';

        state.recordingTimerInterval = setInterval(() => {
            state.recordedDurationSeconds = Math.floor((Date.now() - state.recordingStartTime) / 1000);
            state.recordedHeartRates.push(state.heartRate);

            const timerStr = formatTimer(state.recordedDurationSeconds);
            document.getElementById('recTimerText').innerText = `Recording ● ${timerStr}`;
            document.getElementById('liveRecTimer').innerText = timerStr;
        }, 1000);
    }

    function stopRecording() {
        if (!state.isRecording) return;
        state.isRecording = false;
        clearInterval(state.recordingTimerInterval);

        document.getElementById('recBlinker').classList.add('hidden');
        document.getElementById('markEventBtn').classList.add('hidden');
        document.getElementById('recordBtnLabel').innerText = 'Start Recording';
        document.getElementById('toggleRecordBtn').className = 'pill-btn primary-btn';
        document.getElementById('liveRecBadge').classList.add('hidden');
        document.getElementById('liveRecordBtn').innerText = 'Record';
        document.getElementById('liveRecordBtn').className = 'pill-btn primary-btn';
        document.getElementById('recTimerText').innerText = 'Ready to record';

        const hrList = state.recordedHeartRates;
        const avgHr = hrList.length > 0 ? Math.round(hrList.reduce((a, b) => a + b, 0) / hrList.length) : state.heartRate;
        const minHr = hrList.length > 0 ? Math.min(...hrList) : state.minHeartRate;
        const maxHr = hrList.length > 0 ? Math.max(...hrList) : state.maxHeartRate;

        const newSession = {
            id: Date.now(),
            title: `ECG Session #${state.sessions.length + 1}`,
            date: new Date().toLocaleString(),
            durationSeconds: Math.max(1, state.recordedDurationSeconds),
            avgHeartRate: avgHr,
            minHeartRate: minHr,
            maxHeartRate: maxHr,
            lead: state.selectedLead,
            deviceName: state.isDemoMode ? 'Demo Simulator' : state.deviceName,
            spo2: state.spo2,
            bp: `${state.bpSys}/${state.bpDia}`,
            temp: state.tempC,
            isDemo: state.isDemoMode,
            samples: state.recordedSamples.slice(),
            markers: state.recordedMarkers.slice()
        };

        state.sessions.unshift(newSession);
        saveSessions();
        alert(`Session recorded and saved successfully (${formatTimer(newSession.durationSeconds)})! View it in the History tab.`);
    }

    function markEvent() {
        if (!state.isRecording) return;
        const note = prompt('Enter Clinical Marker Note (e.g. Cough, Palpitation, Exercise):', 'Symptom Reported');
        if (note) {
            state.recordedMarkers.push({
                timeOffset: formatTimer(state.recordedDurationSeconds),
                note: note,
                hr: state.heartRate
            });
        }
    }

    function formatTimer(seconds) {
        const mins = Math.floor(seconds / 60).toString().padStart(2, '0');
        const secs = (seconds % 60).toString().padStart(2, '0');
        return `${mins}:${secs}`;
    }

    // Render History Sessions
    function renderHistoryList() {
        const listEl = document.getElementById('sessionList');
        const countEl = document.getElementById('sessionCountText');
        if (!listEl) return;

        countEl.innerText = `${state.sessions.length} saved sessions`;

        if (state.sessions.length === 0) {
            listEl.innerHTML = `
                <div style="text-align: center; padding: 40px 10px; color: var(--text-tertiary);">
                    <div style="font-size: 32px; margin-bottom: 8px;">📋</div>
                    <strong>No Recorded Sessions</strong>
                    <p style="font-size: 12px; margin-top: 4px;">Record a session from Dashboard or Live ECG tab.</p>
                </div>
            `;
            return;
        }

        listEl.innerHTML = state.sessions.map((s, idx) => `
            <div class="session-item-card" onclick="window.openSessionModal(${s.id})">
                <div class="s-item-top">
                    <div>
                        <div class="s-date">${s.title}</div>
                        <div class="s-meta">${s.date} • ${s.deviceName} (${s.lead})</div>
                    </div>
                    <div class="s-duration">${formatTimer(s.durationSeconds)}</div>
                </div>
                <div class="s-stats-inline">
                    <span>Avg HR: <strong style="color: var(--phosphor-green);">${s.avgHeartRate} BPM</strong></span>
                    <span>Range: <strong>${s.minHeartRate} - ${s.maxHeartRate} BPM</strong></span>
                    <button class="pill-btn primary-btn" style="padding: 4px 10px; font-size: 11px;">View</button>
                </div>
            </div>
        `).join('');
    }

    // Session Modal & Waveform Review
    let activeSession = null;
    window.openSessionModal = function (sessionId) {
        activeSession = state.sessions.find(s => s.id === sessionId);
        if (!activeSession) return;

        document.getElementById('modalSessionTitle').innerText = activeSession.title;
        document.getElementById('modalSessionDate').innerText = `${activeSession.date} • ${activeSession.deviceName}`;
        document.getElementById('mDuration').innerText = formatTimer(activeSession.durationSeconds);
        document.getElementById('mLead').innerText = activeSession.lead;
        document.getElementById('mAvgHr').innerText = `${activeSession.avgHeartRate} BPM`;
        document.getElementById('mRangeHr').innerText = `${activeSession.minHeartRate} - ${activeSession.maxHeartRate} BPM`;
        document.getElementById('mDevice').innerText = activeSession.deviceName;
        document.getElementById('mSpo2').innerText = `${activeSession.spo2 || 98}%`;

        document.getElementById('sessionDetailModal').classList.remove('hidden');
        renderReviewCanvas(0);
    };

    function renderReviewCanvas(offsetRatio) {
        if (!activeSession || !ctxReview || !canvasReview) return;

        const width = canvasReview.width = canvasReview.clientWidth * window.devicePixelRatio;
        const height = canvasReview.height = canvasReview.clientHeight * window.devicePixelRatio;
        const centerY = height / 2;

        ctxReview.fillStyle = '#030609';
        ctxReview.fillRect(0, 0, width, height);

        drawMedicalGrid(ctxReview, width, height);

        const samples = activeSession.samples;
        if (!samples || samples.length === 0) return;

        const windowSize = Math.min(600, samples.length);
        const start = Math.floor((samples.length - windowSize) * offsetRatio);
        const slice = samples.slice(start, start + windowSize);

        const mvScale = (height * 0.35);
        const stepX = width / (slice.length - 1);

        ctxReview.strokeStyle = '#00E676';
        ctxReview.lineWidth = 2.0 * window.devicePixelRatio;
        ctxReview.beginPath();

        slice.forEach((mv, i) => {
            const px = i * stepX;
            const py = centerY - (mv * mvScale);
            if (i === 0) ctxReview.moveTo(px, py);
            else ctxReview.lineTo(px, py);
        });
        ctxReview.stroke();
    }

    // PDF Exporter (Clinical Report)
    function exportPdf(session) {
        const printWindow = window.open('', '_blank');
        if (!printWindow) {
            alert('Please allow popups to export the PDF report.');
            return;
        }

        printWindow.document.write(`
            <!DOCTYPE html>
            <html>
            <head>
                <title>CardioConnect Medical Telemetry Report - ${session.title}</title>
                <style>
                    body { font-family: 'Helvetica Neue', Arial, sans-serif; padding: 40px; color: #111; }
                    .header { border-bottom: 3px solid #00E676; padding-bottom: 12px; margin-bottom: 24px; display: flex; justify-content: space-between; align-items: flex-end; }
                    .title { font-size: 26px; font-weight: bold; margin: 0; color: #0A0E17; }
                    .sub { font-size: 13px; color: #00A34D; font-weight: bold; text-transform: uppercase; }
                    .grid-table { width: 100%; border-collapse: collapse; margin-bottom: 24px; }
                    .grid-table td { padding: 8px 12px; border: 1px solid #ddd; font-size: 13px; }
                    .label { font-weight: bold; color: #555; width: 25%; background: #F8F9FA; }
                    .ecg-box { border: 2px solid #D81B60; background: #FFF5F7; height: 320px; position: relative; margin-bottom: 24px; }
                    .notice { background: #FFF8E1; border: 1px solid #FFE082; padding: 14px; border-radius: 8px; font-size: 11px; color: #665; line-height: 16px; }
                    @media print { button { display: none; } }
                </style>
            </head>
            <body>
                <div class="header">
                    <div>
                        <h1 class="title">CardioConnect</h1>
                        <span class="sub">Clinical-Grade ECG Telemetry Report</span>
                    </div>
                    <div style="font-size: 12px; color: #777;">Report Date: ${new Date().toLocaleString()}</div>
                </div>

                <table class="grid-table">
                    <tr>
                        <td class="label">Patient Name</td><td><strong>${state.patient.fullName}</strong></td>
                        <td class="label">Patient ID &amp; Demographics</td><td>${state.patient.patientId} • ${state.patient.age}y, ${state.patient.gender} (${state.patient.bloodGroup})</td>
                    </tr>
                    <tr>
                        <td class="label">Attending Clinician</td><td>${state.patient.clinician}</td>
                        <td class="label">Allergies / Meds</td><td>Allergies: ${state.patient.allergies} | Meds: ${state.patient.medications}</td>
                    </tr>
                    <tr>
                        <td class="label">Session Title &amp; Date</td><td>${session.title} (${session.date})</td>
                        <td class="label">Monitoring Duration</td><td>${formatTimer(session.durationSeconds)}</td>
                    </tr>
                    <tr>
                        <td class="label">ECG Intervals</td><td colspan="3"><strong>PR:</strong> 160 ms &nbsp;|&nbsp; <strong>QRS:</strong> 92 ms &nbsp;|&nbsp; <strong>QT:</strong> 380 ms &nbsp;|&nbsp; <strong>QTc:</strong> 410 ms &nbsp;|&nbsp; <strong>RR:</strong> 770 ms</td>
                    </tr>
                    <tr>
                        <td class="label">Heart Rate Telemetry</td><td>Avg: <strong>${session.avgHeartRate} BPM</strong> (Range: ${session.minHeartRate} - ${session.maxHeartRate} BPM)</td>
                        <td class="label">Vitals (SpO₂ / BP)</td><td>SpO₂: ${session.spo2 || 98}% &nbsp;|&nbsp; BP: ${session.bp || '120/80 mmHg'}</td>
                    </tr>
                    <tr>
                        <td class="label">Device &amp; Calibration</td><td colspan="3">${session.deviceName} &nbsp;•&nbsp; Lead: ${session.lead} (25 mm/s, 10 mm/mV)</td>
                    </tr>
                </table>

                <h3 style="font-size: 14px; margin-bottom: 8px; text-transform: uppercase;">Calibrated Waveform Trace (${session.lead})</h3>
                <div class="ecg-box">
                    <canvas id="pdfCanvas" style="width: 100%; height: 100%;"></canvas>
                </div>

                <div class="notice">
                    <strong>REGULATORY &amp; MEDICAL SAFETY NOTICE:</strong><br>
                    Data displayed by this application is for monitoring/information purposes and is not a medical diagnosis. CardioConnect prototype software does not diagnose cardiac pathologies or replace physician evaluation.
                </div>

                <script>
                    const c = document.getElementById('pdfCanvas');
                    const ctx = c.getContext('2d');
                    c.width = c.clientWidth * 2;
                    c.height = c.clientHeight * 2;
                    const w = c.width, h = c.height;

                    // Draw pink clinical millimeter grid
                    ctx.strokeStyle = '#FCE4EC';
                    ctx.lineWidth = 1;
                    for (let x=0; x<=w; x+=20) { ctx.beginPath(); ctx.moveTo(x,0); ctx.lineTo(x,h); ctx.stroke(); }
                    for (let y=0; y<=h; y+=20) { ctx.beginPath(); ctx.moveTo(0,y); ctx.lineTo(w,y); ctx.stroke(); }

                    ctx.strokeStyle = '#F8BBD0';
                    ctx.lineWidth = 2;
                    for (let x=0; x<=w; x+=100) { ctx.beginPath(); ctx.moveTo(x,0); ctx.lineTo(x,h); ctx.stroke(); }
                    for (let y=0; y<=h; y+=100) { ctx.beginPath(); ctx.moveTo(0,y); ctx.lineTo(w,y); ctx.stroke(); }

                    // Draw vector trace in crisp black
                    const samples = ${JSON.stringify(session.samples.slice(0, 1000))};
                    ctx.strokeStyle = '#000000';
                    ctx.lineWidth = 2.5;
                    ctx.beginPath();
                    const centerY = h / 2;
                    const step = w / (samples.length - 1);
                    samples.forEach((v, i) => {
                        const px = i * step;
                        const py = centerY - (v * (h * 0.35));
                        if (i === 0) ctx.moveTo(px, py);
                        else ctx.lineTo(px, py);
                    });
                    ctx.stroke();

                    setTimeout(() => window.print(), 500);
                <\/script>
            </body>
            </html>
        `);
        printWindow.document.close();
    }

    // CSV Exporter
    function exportCsv(session) {
        const rows = [
            ['# CardioConnect ECG Telemetry Export'],
            [`# Session: ${session.title}`],
            [`# Date: ${session.date}`],
            [`# Device: ${session.deviceName}`],
            [`# Lead: ${session.lead}`],
            [`# DurationSec: ${session.durationSeconds}`],
            [`# AvgHR: ${session.avgHeartRate}, MinHR: ${session.minHeartRate}, MaxHR: ${session.maxHeartRate}`],
            ['# Notice: For monitoring/informational purposes only. Not a medical diagnosis.'],
            [],
            ['SampleIndex', 'TimeOffsetSec', 'Voltage_mV', 'Lead', 'HeartRate_BPM']
        ];

        const samples = session.samples || [];
        const interval = session.durationSeconds / Math.max(1, samples.length);

        samples.forEach((mv, idx) => {
            const timeOffset = (idx * interval).toFixed(4);
            rows.push([idx, timeOffset, mv, session.lead, session.avgHeartRate]);
        });

        const csvContent = 'data:text/csv;charset=utf-8,' + rows.map(r => r.join(',')).join('\n');
        const encodedUri = encodeURI(csvContent);
        const link = document.createElement('a');
        link.setAttribute('href', encodedUri);
        link.setAttribute('download', `CardioConnect_${session.id}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

    // Bluetooth Hardware Scanner Simulation / Web Bluetooth
    const mockDevices = [
        { name: 'ECG-Monitor-01', addr: 'CC:52:90:3A:41:F0', rssi: -58, battery: 92, connected: false },
        { name: 'ECG-Monitor-02', addr: 'B8:27:EB:11:9C:A4', rssi: -72, battery: 84, connected: false },
        { name: 'CardioPatch-Pro', addr: 'D4:F5:13:88:20:09', rssi: -64, battery: 67, connected: false }
    ];

    function renderDeviceList() {
        const listEl = document.getElementById('deviceList');
        if (!listEl) return;

        listEl.innerHTML = mockDevices.map((d, idx) => `
            <div class="device-card">
                <div>
                    <div class="dev-name">${d.name} ${d.connected ? '<span style="color: var(--phosphor-green); font-size: 11px;">(CONNECTED)</span>' : ''}</div>
                    <span class="dev-addr">${d.addr} • Battery: ${d.battery}%</span>
                    <span class="dev-rssi">Signal: ${d.rssi} dBm</span>
                </div>
                ${d.connected ? `
                    <button class="pill-btn danger-btn" onclick="window.disconnectBle()">Disconnect</button>
                ` : `
                    <button class="pill-btn primary-btn" onclick="window.connectBle(${idx})">Connect</button>
                `}
            </div>
        `).join('');
    }

    window.connectBle = function (idx) {
        mockDevices.forEach(d => d.connected = false);
        const target = mockDevices[idx];
        target.connected = true;

        state.isBleConnected = true;
        state.deviceName = target.name;
        state.battery = target.battery;
        state.rssi = target.rssi;
        state.isDemoMode = false;
        document.getElementById('demoModeToggle').checked = false;

        document.getElementById('connectedDeviceBox').classList.remove('hidden');
        document.getElementById('connDevName').innerText = target.name;
        document.getElementById('connDevAddr').innerText = target.addr;

        updateVitalsUI();
        renderDeviceList();
        alert(`Connected to ${target.name} via Bluetooth LE! Real telemetry active.`);
    };

    window.disconnectBle = function () {
        mockDevices.forEach(d => d.connected = false);
        state.isBleConnected = false;
        state.isDemoMode = true;
        document.getElementById('demoModeToggle').checked = true;

        document.getElementById('connectedDeviceBox').classList.add('hidden');
        updateVitalsUI();
        renderDeviceList();
    };

    // Event Handlers Setup
    function setupEvents() {
        // Navigation Tabs
        const navButtons = document.querySelectorAll('.nav-item');
        const tabPanes = document.querySelectorAll('.tab-pane');

        navButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                navButtons.forEach(b => b.classList.remove('active'));
                tabPanes.forEach(p => p.classList.remove('active'));

                btn.classList.add('active');
                const targetId = btn.getAttribute('data-tab');
                const targetPane = document.getElementById(targetId);
                if (targetPane) targetPane.classList.add('active');
            });
        });

        // Dashboard Pause
        document.getElementById('dashPauseBtn').addEventListener('click', () => {
            state.isPaused = !state.isPaused;
            document.getElementById('dashPauseText').innerText = state.isPaused ? 'Resume' : 'Pause';
            document.getElementById('livePauseBtn').innerText = state.isPaused ? 'Resume' : 'Pause';
        });

        document.getElementById('livePauseBtn').addEventListener('click', () => {
            state.isPaused = !state.isPaused;
            document.getElementById('dashPauseText').innerText = state.isPaused ? 'Resume' : 'Pause';
            document.getElementById('livePauseBtn').innerText = state.isPaused ? 'Resume' : 'Pause';
        });

        // Expand to Live Tab
        document.getElementById('dashFullscreenBtn').addEventListener('click', () => {
            document.querySelector('[data-tab="tabLive"]').click();
        });

        // Recording Buttons
        document.getElementById('toggleRecordBtn').addEventListener('click', () => {
            if (state.isRecording) stopRecording();
            else startRecording();
        });

        document.getElementById('liveRecordBtn').addEventListener('click', () => {
            if (state.isRecording) stopRecording();
            else startRecording();
        });

        document.getElementById('markEventBtn').addEventListener('click', markEvent);
        document.getElementById('liveMarkBtn').addEventListener('click', markEvent);

        // Calibration Selectors
        document.getElementById('leadSelect').addEventListener('change', (e) => {
            state.selectedLead = e.target.value;
            document.getElementById('dashLeadBadge').innerText = e.target.options[e.target.selectedIndex].text.toUpperCase();
        });

        document.getElementById('speedSelect').addEventListener('change', (e) => {
            state.sweepSpeed = parseFloat(e.target.value);
            document.getElementById('dashCalibBadge').innerText = `${state.sweepSpeed} mm/s • ${state.gain} mm/mV`;
        });

        document.getElementById('gainSelect').addEventListener('change', (e) => {
            state.gain = parseFloat(e.target.value);
            document.getElementById('dashCalibBadge').innerText = `${state.sweepSpeed} mm/s • ${state.gain} mm/mV`;
        });

        // Settings Toggles
        document.getElementById('demoModeToggle').addEventListener('change', (e) => {
            state.isDemoMode = e.target.checked;
            updateVitalsUI();
        });

        document.getElementById('gridToggle').addEventListener('change', (e) => {
            state.showGrid = e.target.checked;
        });

        document.getElementById('colorSelect').addEventListener('change', (e) => {
            state.traceColor = e.target.value;
        });

        // Device Scan Button
        document.getElementById('scanBleBtn').addEventListener('click', () => {
            const btnText = document.getElementById('scanBtnText');
            btnText.innerText = 'Scanning...';
            setTimeout(() => {
                btnText.innerText = 'Scan for Devices';
                renderDeviceList();
            }, 1200);
        });

        document.getElementById('disconnectBleBtn').addEventListener('click', window.disconnectBle);

        // Modal Controls
        document.getElementById('closeModalBtn').addEventListener('click', () => {
            document.getElementById('sessionDetailModal').classList.add('hidden');
        });

        document.getElementById('scrubSlider').addEventListener('input', (e) => {
            const val = parseFloat(e.target.value) / 100;
            if (activeSession) {
                const timeSec = (val * activeSession.durationSeconds).toFixed(1);
                document.getElementById('scrubTimeLabel').innerText = `${timeSec}s`;
            }
            renderReviewCanvas(val);
        });

        document.getElementById('exportPdfBtn').addEventListener('click', () => {
            if (activeSession) exportPdf(activeSession);
        });

        document.getElementById('exportCsvBtn').addEventListener('click', () => {
            if (activeSession) exportCsv(activeSession);
        });

        document.getElementById('clearHistoryBtn').addEventListener('click', () => {
            if (confirm('Delete all recorded ECG sessions?')) {
                state.sessions = [];
                saveSessions();
            }
        });

        // Patient Edit Modal
        const patientModal = document.getElementById('patientEditModal');
        document.getElementById('openPatientEditBtn').addEventListener('click', () => {
            const p = state.patient;
            document.getElementById('inputPatName').value = p.fullName;
            document.getElementById('inputPatId').value = p.patientId;
            document.getElementById('inputPatAge').value = p.age;
            document.getElementById('inputPatGender').value = p.gender;
            document.getElementById('inputPatDob').value = p.dob;
            document.getElementById('inputPatBlood').value = p.bloodGroup;
            document.getElementById('inputPatHeight').value = p.heightCm;
            document.getElementById('inputPatWeight').value = p.weightKg;
            document.getElementById('inputPatClinician').value = p.clinician;
            document.getElementById('inputPatAllergies').value = p.allergies;
            document.getElementById('inputPatMeds').value = p.medications;
            document.getElementById('inputPatNotes').value = p.notes;
            document.getElementById('inputPatEmergency').value = p.emergency;

            patientModal.classList.remove('hidden');
        });

        document.getElementById('closePatientModalBtn').addEventListener('click', () => {
            patientModal.classList.add('hidden');
        });
        document.getElementById('cancelPatientBtn').addEventListener('click', () => {
            patientModal.classList.add('hidden');
        });

        document.getElementById('savePatientBtn').addEventListener('click', () => {
            const p = getActivePatient();
            p.fullName = document.getElementById('inputPatName').value || p.fullName;
            p.patientId = document.getElementById('inputPatId').value || p.patientId;
            p.age = parseInt(document.getElementById('inputPatAge').value) || p.age;
            p.gender = document.getElementById('inputPatGender').value || p.gender;
            p.dob = document.getElementById('inputPatDob').value || p.dob;
            p.bloodGroup = document.getElementById('inputPatBlood').value || p.bloodGroup;
            p.heightCm = parseFloat(document.getElementById('inputPatHeight').value) || p.heightCm;
            p.weightKg = parseFloat(document.getElementById('inputPatWeight').value) || p.weightKg;
            p.clinician = document.getElementById('inputPatClinician').value || p.clinician;
            p.allergies = document.getElementById('inputPatAllergies').value || p.allergies;
            p.medications = document.getElementById('inputPatMeds').value || p.medications;
            p.notes = document.getElementById('inputPatNotes').value || p.notes;
            p.emergency = document.getElementById('inputPatEmergency').value || p.emergency;

            state.patient = p;
            savePatients();
            patientModal.classList.add('hidden');
        });

        // Quick Switch Patient from Dashboard
        const quickSwitchBtn = document.getElementById('quickSwitchPatientBtn');
        if (quickSwitchBtn) {
            quickSwitchBtn.addEventListener('click', () => {
                switchTab('tabPatients');
            });
        }

        // Patient Search Filter
        const patSearchInput = document.getElementById('patientSearchInput');
        if (patSearchInput) {
            patSearchInput.addEventListener('input', (e) => {
                renderPatientsList(e.target.value);
            });
        }

        // Add Patient Modal Handlers
        const addPatModal = document.getElementById('addPatientModal');
        const addPatError = document.getElementById('addPatientError');

        function updateNewPatBmi() {
            const h = parseFloat(document.getElementById('newPatHeight').value);
            const w = parseFloat(document.getElementById('newPatWeight').value);
            const bmiEl = document.getElementById('newPatBmi');
            if (bmiEl) {
                const info = calculateBmi(h, w);
                bmiEl.innerText = info.text;
                bmiEl.style.color = (info.cat === 'Normal weight') ? 'var(--phosphor-green)' : (info.cat === 'Overweight') ? 'var(--medical-amber)' : 'var(--text-primary)';
            }
        }

        document.getElementById('newPatHeight').addEventListener('input', updateNewPatBmi);
        document.getElementById('newPatWeight').addEventListener('input', updateNewPatBmi);

        document.getElementById('openAddPatientModalBtn').addEventListener('click', () => {
            document.getElementById('newPatId').value = getNextSuggestedPatientId();
            document.getElementById('newPatName').value = '';
            document.getElementById('newPatAge').value = '';
            document.getElementById('newPatDob').value = '';
            document.getElementById('newPatHeight').value = '';
            document.getElementById('newPatWeight').value = '';
            document.getElementById('newPatBmi').innerText = '—';
            document.getElementById('newPatPhone').value = '';
            document.getElementById('newPatEmergency').value = '';
            document.getElementById('newPatNotes').value = '';
            document.getElementById('newPatMeds').value = '';
            document.getElementById('newPatAllergies').value = '';
            addPatError.classList.add('hidden');
            addPatModal.classList.remove('hidden');
        });

        document.getElementById('closeAddPatientModalBtn').addEventListener('click', () => {
            addPatModal.classList.add('hidden');
        });
        document.getElementById('cancelAddPatientBtn').addEventListener('click', () => {
            addPatModal.classList.add('hidden');
        });

        document.getElementById('saveNewPatientBtn').addEventListener('click', () => {
            const id = (document.getElementById('newPatId').value || '').trim();
            const name = (document.getElementById('newPatName').value || '').trim();
            const age = parseInt(document.getElementById('newPatAge').value, 10);
            const gender = document.getElementById('newPatGender').value;
            const dob = document.getElementById('newPatDob').value.trim() || 'Unspecified';
            const h = parseFloat(document.getElementById('newPatHeight').value) || 0;
            const w = parseFloat(document.getElementById('newPatWeight').value) || 0;
            const blood = document.getElementById('newPatBlood').value;
            const phone = document.getElementById('newPatPhone').value.trim() || 'Unspecified';
            const emergency = document.getElementById('newPatEmergency').value.trim() || 'Unspecified';
            const clinician = document.getElementById('newPatClinician').value.trim() || 'Unassigned';
            const notes = document.getElementById('newPatNotes').value.trim() || 'None recorded';
            const meds = document.getElementById('newPatMeds').value.trim() || 'None recorded';
            const allergies = document.getElementById('newPatAllergies').value.trim() || 'None reported';

            if (!id) {
                addPatError.innerText = 'Patient ID is required.';
                addPatError.classList.remove('hidden');
                return;
            }
            if (!name) {
                addPatError.innerText = 'Patient Full Name is required.';
                addPatError.classList.remove('hidden');
                return;
            }
            if (isNaN(age) || age <= 0) {
                addPatError.innerText = 'Please enter a valid age.';
                addPatError.classList.remove('hidden');
                return;
            }

            // Check duplicate ID
            if (state.patients.some(p => p.patientId.toLowerCase() === id.toLowerCase())) {
                addPatError.innerText = `Patient ID "${id}" is already registered. Please choose a unique ID.`;
                addPatError.classList.remove('hidden');
                return;
            }

            const newPatient = {
                patientId: id,
                fullName: name,
                age: age,
                gender: gender,
                dob: dob,
                bloodGroup: blood,
                heightCm: h,
                weightKg: w,
                phone: phone,
                emergency: emergency,
                clinician: clinician,
                notes: notes,
                medications: meds,
                allergies: allergies,
                totalSessions: 0,
                lastEcgTime: 'Never'
            };

            state.patients.push(newPatient);
            executePatientSwitch(id);
            addPatModal.classList.add('hidden');
            switchTab('tabPatients');
        });

        // Patient Profile Detail Modal
        const profModal = document.getElementById('patientProfileDetailModal');
        document.getElementById('closeProfileDetailModalBtn').addEventListener('click', () => {
            profModal.classList.add('hidden');
        });

        document.getElementById('profSetMonitorBtn').addEventListener('click', () => {
            if (viewingProfilePatientId) {
                attemptPatientSwitch(viewingProfilePatientId, true);
                profModal.classList.add('hidden');
            }
        });

        document.getElementById('profEditBtn').addEventListener('click', () => {
            profModal.classList.add('hidden');
            if (viewingProfilePatientId) {
                const p = state.patients.find(pat => pat.patientId === viewingProfilePatientId) || getActivePatient();
                document.getElementById('inputPatName').value = p.fullName;
                document.getElementById('inputPatId').value = p.patientId;
                document.getElementById('inputPatAge').value = p.age;
                document.getElementById('inputPatGender').value = p.gender;
                document.getElementById('inputPatDob').value = p.dob;
                document.getElementById('inputPatBlood').value = p.bloodGroup;
                document.getElementById('inputPatHeight').value = p.heightCm;
                document.getElementById('inputPatWeight').value = p.weightKg;
                document.getElementById('inputPatClinician').value = p.clinician;
                document.getElementById('inputPatAllergies').value = p.allergies;
                document.getElementById('inputPatMeds').value = p.medications;
                document.getElementById('inputPatNotes').value = p.notes;
                document.getElementById('inputPatEmergency').value = p.emergency;
                patientModal.classList.remove('hidden');
            }
        });

        document.getElementById('profDeleteBtn').addEventListener('click', () => {
            if (state.patients.length <= 1) {
                alert('At least one patient profile must remain in the system.');
                return;
            }
            const p = state.patients.find(pat => pat.patientId === viewingProfilePatientId);
            if (!p) return;
            if (confirm(`Delete patient profile for ${p.fullName} (${p.patientId})? This action cannot be undone.`)) {
                state.patients = state.patients.filter(pat => pat.patientId !== p.patientId);
                if (state.activePatientId === p.patientId) {
                    state.activePatientId = state.patients[0].patientId;
                }
                savePatients();
                profModal.classList.add('hidden');
            }
        });

        // Safety Guard Modal Handlers
        const safetyModal = document.getElementById('safetyGuardModal');
        document.getElementById('closeSafetyGuardBtn').addEventListener('click', () => {
            safetyModal.classList.add('hidden');
            pendingSwitchPatientId = null;
        });
        document.getElementById('cancelGuardBtn').addEventListener('click', () => {
            safetyModal.classList.add('hidden');
            pendingSwitchPatientId = null;
        });
        document.getElementById('confirmStopAndSwitchBtn').addEventListener('click', () => {
            if (state.isRecording) {
                stopRecording();
            }
            if (pendingSwitchPatientId) {
                executePatientSwitch(pendingSwitchPatientId);
                pendingSwitchPatientId = null;
            }
            safetyModal.classList.add('hidden');
            switchTab('tabLive');
        });

        // Diagnostic Explanation Modal
        const diagModal = document.getElementById('diagnosticExplanationModal');
        document.getElementById('closeDiagModalBtn').addEventListener('click', () => {
            diagModal.classList.add('hidden');
        });
        document.getElementById('ackDiagBtn').addEventListener('click', () => {
            diagModal.classList.add('hidden');
        });

        // ECG Conditions Educational Guide Modal
        const guideModal = document.getElementById('conditionsGuideModal');
        document.getElementById('openConditionsGuideBtn').addEventListener('click', () => {
            guideModal.classList.remove('hidden');
        });
        document.getElementById('closeGuideModalBtn').addEventListener('click', () => {
            guideModal.classList.add('hidden');
        });
        document.getElementById('ackGuideBtn').addEventListener('click', () => {
            guideModal.classList.add('hidden');
        });

        // Dismiss Alert
        document.getElementById('dismissAlertBtn').addEventListener('click', () => {
            document.getElementById('activeAlert').classList.add('hidden');
        });

        // Fullscreen view toggle
        document.getElementById('viewModeBtn').addEventListener('click', () => {
            if (!document.fullscreenElement) {
                document.documentElement.requestFullscreen().catch(() => {});
            } else {
                document.exitFullscreen().catch(() => {});
            }
        });
    }

    // Startup
    initStorage();
    renderDeviceList();
    setupEvents();
    updateVitalsUI();
    requestAnimationFrame(animationLoop);
})();
