-- ============================================================
-- Seed capteurs - Zones Bamako
-- Table cible: public.sensors
-- Rejouable sans doublons (ON CONFLICT)
-- ============================================================

INSERT INTO public.sensors (
  id,
  statut,
  localite,
  latitude,
  longitude,
  "timestamp"
)
VALUES
  (
    'SENSOR_SEBENIKORO_01',
    'active',
    'Quartier de Sebenikoro, Bamako',
    12.613138774011157,
    -8.045667553387089,
    NOW()
  ),
  (
    'SENSOR_YIRIMADIO_01',
    'active',
    'Quartier de Yirimadio, Bamako',
    12.607504405292698,
    -7.923320997395195,
    NOW()
  ),
  (
    'SENSOR_MISSABOUGOU_01',
    'active',
    'Quartier de Missabougou, Bamako',
    12.633253847822019,
    -7.9186749686637485,
    NOW()
  )
ON CONFLICT (id) DO UPDATE
SET
  statut = EXCLUDED.statut,
  localite = EXCLUDED.localite,
  latitude = EXCLUDED.latitude,
  longitude = EXCLUDED.longitude,
  "timestamp" = NOW(),
  updated_at = NOW();

