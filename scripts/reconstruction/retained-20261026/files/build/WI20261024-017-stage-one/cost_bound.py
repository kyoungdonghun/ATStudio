"""Pure cost worksheet. Never produces an approval or provider assertion."""
import math
import time

MIN_CHUNK=512*1024
ZSTD_BLOCK=64*1024
NODE_FIXED=8192


def need(ok):
    if not ok:raise ValueError('COST_BOUND_HOLD')


def number(value,maximum=2**63-1):
    need(type(value) is int and 0<=value<=maximum);return value


def compressed_bound(size,blobs):
    """v1.18.6 MaxEncodedSize, no dictionary/CRC/padding; 64KiB conservative block."""
    return size+17*blobs+3*(size//ZSTD_BLOCK)


def packs(size,blobs):
    # 32 encryption + 41 header entry + <=one 36-byte pack trailer per blob.
    return compressed_bound(size,blobs)+(32+41+36)*blobs


def calculate(metadata,provider,format_proof,now=None,admitted_at=None):
    """Inputs are real measured metadata/provider evidence, not code-created facts."""
    now=int(time.time()) if now is None else now
    need(metadata['status']=='BOUNDED_COST_METADATA_OBSERVED' and metadata['xattrs_absent'] is True
        and metadata['generic_attributes']=='LINUX_NONE' and metadata['basename_utf8_bytes_max']==255
        and metadata['local_owner_group_utf8_bytes_max']==256)
    for key in ('source_bytes','files','directories','ancestors','manifest_bytes','sql_bytes_bound','sql_proof_bytes_bound','payload_files','payload_directories'):
        number(metadata[key])
    need(metadata['payload_files']==4 and metadata['payload_directories']==3
        and metadata['sql_bytes_bound']==4*1024**2 and metadata['sql_proof_bytes_bound']==16384)
    need(format_proof==dict(restic='0.19.1',zstd='1.18.6',linux_generic_attributes_absent=True,
        new_pack_index_entries_once=True,index_entry_bytes_max=512,index_file_envelope_bytes_max=256,
        snapshot_lock_plaintext_bytes_max=65536,allocation_unit_bytes=4096,
        maximum_operation_seconds=1800,restic_commands=5,lock_refresh_seconds=300))
    need(format_proof['linux_generic_attributes_absent'] is True and format_proof['new_pack_index_entries_once'] is True)
    size=metadata['source_bytes']+metadata['sql_bytes_bound']+metadata['manifest_bytes']+metadata['sql_proof_bytes_bound']
    file_count=metadata['files']+metadata['payload_files']
    data_blobs=math.ceil(size/MIN_CHUNK)+file_count
    nodes=metadata['files']+metadata['directories']+metadata['ancestors']+metadata['payload_files']+metadata['payload_directories']
    # Every directory/ancestor can create one tree, including synthetic absolute-path ancestors.
    trees=metadata['directories']+metadata['ancestors']+metadata['payload_directories']+1
    tree_size=NODE_FIXED*nodes+67*data_blobs+32*trees
    blob_count=data_blobs+trees
    # Worst case: one pack and one index file per blob. No dedup/compression saving.
    index_size=512*blob_count+256*blob_count
    indexes=blob_count
    # Five commands: <=two initial lock saves, six periodic refreshes and two stale-refresh
    # allowances per command, plus one snapshot. Assume no old lock deletion saves space.
    auxiliary_files=5*(2+6+2)+1
    auxiliary_size=65536*auxiliary_files
    objects=blob_count+indexes+auxiliary_files
    new_bytes=packs(size,data_blobs)+packs(tree_size,trees)
    new_bytes+=compressed_bound(index_size,indexes)+33*indexes
    new_bytes+=compressed_bound(auxiliary_size,auxiliary_files)+33*auxiliary_files
    new_bytes+=4095*objects
    need(type(provider) is dict and provider['currency']=='KRW')
    observed=number(provider['observed_at']);number(provider['expires_at'])
    admission=now if admitted_at is None else number(admitted_at)
    need(0<=admission-observed<=600 and admission<=now<provider['expires_at'])
    existing=number(provider['repository_actual_bytes']);included=number(provider['storage_included_bytes'])
    other_stored=number(provider['account_other_stored_bytes'])
    headroom=number(provider['other_growth_reserved_bytes'])
    need(provider['concurrent_writers_absent'] is True and provider['account_allowance_verified'] is True)
    need(provider['api_currently_free'] is True and provider['same_provider_internal_traffic_free'] is True
        and provider['same_internal_endpoint_verified'] is True and provider['tax_on_zero_is_zero'] is True
        and provider['retained_storage_current_terms_continue'] is True)
    total=existing+other_stored+headroom+new_bytes
    return dict(status='WITHIN_CURRENT_INCLUDED_STORAGE' if total<=included else 'HOLD_STORAGE_BOUND',
        authorization='NOT_ISSUED',additional_paid_budget_krw=0,dedup_savings_assumed=0,
        compression_savings_assumed=0,source_payload_bytes_bound=size,data_blob_count_bound=data_blobs,
        tree_plaintext_bytes_bound=tree_size,index_plaintext_bytes_bound=index_size,
        new_repository_bytes_bound=new_bytes,retained_repository_bytes_bound=total,
        storage_included_bytes=included,remaining_included_bytes=included-total,
        api_and_internal_egress='CURRENT_ZERO_TARIFF_REQUIRED',no_deletion_assumed=True,
        historical_snapshots_retained=True,provider_billing_enforcement=False)
